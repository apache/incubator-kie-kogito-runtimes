/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.maven.plugin;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.Index;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.server.core.admin.embeddedserver.EmbeddedServerAdminOperationHandler;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.kie.kogito.maven.plugin.util.KafkaServer;

import static org.infinispan.commons.dataconversion.MediaType.APPLICATION_PROTOSTREAM_TYPE;

@Mojo(name = "dev", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DevMojo extends AbstractKieMojo {

    private static final String LOCALHOST = "localhost";
    
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    private void startKafka() throws MojoExecutionException {
        try {
            ServerCnxnFactory factory = ServerCnxnFactory.createFactory(new InetSocketAddress(LOCALHOST, 2182), 1024);
            Path data = Files.createTempDirectory("zk");
            Path snap = Files.createDirectory(data.resolve("snap"));
            Path log = Files.createDirectory(data.resolve("log"));
            ZooKeeperServer zkServer = new ZooKeeperServer(snap.toFile(), log.toFile(), 500);
            factory.startup(zkServer);
            KafkaServer server = new KafkaServer(() -> LOCALHOST + ":2182", 9092);
            Path kafkaDir = Files.createDirectories(data.resolve("kafka").resolve("broker" + server.brokerId()));
            server.setStateDirectory(kafkaDir.toFile());
            server.startup();
        } catch (Exception ex) {
            getLog().error(ex);
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }

    private void startInfinispan() {
        Configuration config = new ConfigurationBuilder()
                .template(true)
                .clustering().cacheMode(CacheMode.LOCAL)
                .indexing().index(Index.PRIMARY_OWNER).addProperty("default.directory_provider", "local-heap")
                .encoding().key().mediaType(APPLICATION_PROTOSTREAM_TYPE)
                .encoding().value().mediaType(APPLICATION_PROTOSTREAM_TYPE)
                .build();

        GlobalConfiguration globalConfig = new GlobalConfigurationBuilder()
                .defaultCacheName("default")
                .nonClusteredDefault()
                .build();

        EmbeddedCacheManager cacheManager = new DefaultCacheManager(globalConfig, new ConfigurationBuilder().build());
        cacheManager.defineConfiguration("kogito-template", config);

        HotRodServer hotRodServer = new HotRodServer();
        HotRodServerConfiguration cfg = new HotRodServerConfigurationBuilder()
                .host(LOCALHOST)
                .proxyHost(LOCALHOST)
                .port(11222)
                .proxyPort(11222)
                .adminOperationsHandler(new EmbeddedServerAdminOperationHandler())
                .build();
        hotRodServer.start(cfg, cacheManager);
    }

    private void startDataIndex() throws MojoExecutionException {
        PluginDescriptor plugin = (PluginDescriptor) getPluginContext().get("pluginDescriptor");
        Optional<ComponentDependency> dep = plugin.getDependencies().stream().filter(d -> d.getGroupId().equals("org.kie.kogito") &&
                d.getArtifactId().equals("data-index-service")).findFirst();
        if (dep.isPresent()) {
            ComponentDependency dependency = dep.get();
            String path = session.getLocalRepository().pathOf(new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), null, dependency.getType(), "runner", new DefaultArtifactHandler(dependency.getType())));
            String newPath = session.getLocalRepository().getBasedir() + File.separator + path;

            String outputDirectory = project.getBuild().getOutputDirectory();
            String proto = outputDirectory + File.separator + "persistence";

            String protoFolder = "-Dkogito.protobuf.folder=" + proto;
            getLog().debug("Kogito proto folder: " + protoFolder);
            
            ProcessBuilder builder = new ProcessBuilder("java", protoFolder, "-Dkogito.protobuf.watch=true", "-jar", newPath);
            try {
                builder.directory(new File(project.getBuild().getTestOutputDirectory()));
                builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                Process process = builder.start();
                process.waitFor();
            } catch (Exception ex) {
                getLog().error(ex);
                throw new MojoExecutionException(ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Starting Kogito dev mode");
        startKafka();
        startInfinispan();
        startDataIndex();
    }
}
