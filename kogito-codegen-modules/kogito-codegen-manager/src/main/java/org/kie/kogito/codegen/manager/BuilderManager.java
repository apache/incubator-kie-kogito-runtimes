/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.kie.kogito.codegen.manager;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.codegen.common.GeneratedFile;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.manager.util.CodeGenManagerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuilderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuilderManager.class);

    public interface KogitoBuildContextInfo {
        Path projectBaseAbsolutePath();

        CodeGenManagerUtil.Framework framework();

        boolean enablePersistence();

        Set<URI> projectFilesUris();
    }

    public record ScaffoldInfo(Set<URI> projectFilesUris,
            Path projectBaseAbsolutePath, //MUST BE ABSOLUTE
            String projectGroupId,
            String projectArtifactId,
            String projectVersion,
            boolean generatePartial,
            boolean enablePersistence,
            CodeGenManagerUtil.Framework framework,
            Map<String, String> properties) implements KogitoBuildContextInfo {
    }

    public record BuildInfo(Set<URI> projectFilesUris,
            Path projectBaseAbsolutePath, //MUST BE ABSOLUTE
            Path outputDirectory,
            String projectGroupId,
            String projectArtifactId,
            String projectVersion,
            String javaSourceEncoding,
            String javaVersion,
            String jsonSchemaVersion,
            boolean generatePartial,
            boolean enablePersistence,
            boolean onDemand,
            boolean keepSources,
            List<String> runtimeClassPathElements,
            CodeGenManagerUtil.Framework framework,
            Map<String, String> properties) implements KogitoBuildContextInfo {
    }

    public static void build(BuildInfo buildInfo) throws MalformedURLException {
        LOGGER.info("Building project: {}:{}:{}", buildInfo.projectGroupId(), buildInfo.projectArtifactId(), buildInfo.projectVersion());
        CodeGenManagerUtil.setSystemProperties(buildInfo.properties());
        ClassLoader projectClassLoader = CodeGenManagerUtil.projectClassLoader(buildInfo.projectFilesUris());
        KogitoGAV kogitoGAV = new KogitoGAV(buildInfo.projectGroupId(), buildInfo.projectArtifactId(), buildInfo.projectVersion());
        KogitoBuildContext kogitoBuildContext = getKogitoBuildContext(projectClassLoader, kogitoGAV, buildInfo);
        GenerateModelHelper.GenerateModelInfo generateModelInfo = new GenerateModelHelper.GenerateModelInfo(projectClassLoader,
                kogitoBuildContext, buildInfo);
        GenerateModelHelper.generateModel(generateModelInfo);
        LOGGER.info("Project build done");
    }

    public static void scaffold(ScaffoldInfo scaffoldInfo) throws MalformedURLException {
        LOGGER.info("Building project: {}:{}:{}", scaffoldInfo.projectGroupId(), scaffoldInfo.projectArtifactId(), scaffoldInfo.projectVersion());
        CodeGenManagerUtil.setSystemProperties(scaffoldInfo.properties());
        ClassLoader projectClassLoader = CodeGenManagerUtil.projectClassLoader(scaffoldInfo.projectFilesUris());
        KogitoGAV kogitoGAV = new KogitoGAV(scaffoldInfo.projectGroupId(), scaffoldInfo.projectArtifactId(), scaffoldInfo.projectVersion());
        KogitoBuildContext kogitoBuildContext = getKogitoBuildContext(projectClassLoader, kogitoGAV, scaffoldInfo);
        GenerateModelHelper.GenerateModelFilesInfo generateModelFilesInfo = new GenerateModelHelper.GenerateModelFilesInfo(kogitoBuildContext, scaffoldInfo);
        Map<String, Collection<GeneratedFile>> generatedFiles = GenerateModelHelper.generateModelFiles(generateModelFilesInfo);
        LOGGER.info("Scaffold creation done");
    }

    static KogitoBuildContext getKogitoBuildContext(ClassLoader projectClassLoader, KogitoGAV kogitoGAV, KogitoBuildContextInfo kogitoBuildContextInfo) {
        return CodeGenManagerUtil.discoverKogitoRuntimeContext(projectClassLoader, kogitoBuildContextInfo.projectBaseAbsolutePath(), kogitoGAV,
                new CodeGenManagerUtil.ProjectParameters(kogitoBuildContextInfo.framework(), "", "", "", "", kogitoBuildContextInfo.enablePersistence()),
                className -> {
                    try {
                        return CodeGenManagerUtil.isClassNameInUrlClassLoader(kogitoBuildContextInfo.projectFilesUris(), className);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
