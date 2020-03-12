/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.decision;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.namespace.QName;

import org.drools.core.io.impl.ByteArrayResource;
import org.drools.core.io.impl.FileSystemResource;
import org.drools.core.io.internal.InternalResource;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.assembler.DMNResource;
import org.kie.dmn.model.api.Definitions;
import org.kie.dmn.model.v1_2.TDecision;
import org.kie.internal.io.ResourceWithConfigurationImpl;
import org.kie.kogito.codegen.AbstractGenerator;
import org.kie.kogito.codegen.ApplicationGenerator;
import org.kie.kogito.codegen.ApplicationSection;
import org.kie.kogito.codegen.ConfigGenerator;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.grafana.GrafanaConfigurationWriter;

import static org.drools.core.util.IoUtils.readBytesFromInputStream;
import static org.kie.api.io.ResourceType.determineResourceType;
import static org.kie.kogito.codegen.ApplicationGenerator.log;

public class DecisionCodegen extends AbstractGenerator {

    private String packageName;
    private String applicationCanonicalName;
    private DependencyInjectionAnnotator annotator;

    private DecisionContainerGenerator moduleGenerator;

    private Path basePath;
    private final Map<String, DMNResource> models;
    private final List<GeneratedFile> generatedFiles = new ArrayList<>();
    private boolean useMonitoring = false;

    public DecisionCodegen(Path basePath, Collection<? extends DMNResource> models) {
        this.basePath = basePath;
        this.models = new HashMap<>();
        for (DMNResource model : models) {
            this.models.put(model.getDefinitions().getId(), model);
        }

        // set default package name
        setPackageName(ApplicationGenerator.DEFAULT_PACKAGE_NAME);
        this.moduleGenerator = new DecisionContainerGenerator(applicationCanonicalName, basePath, models);
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
        this.applicationCanonicalName = packageName + ".Application";
    }

    public Path getBasePath() {
        return this.basePath;
    }

    public void setDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
    }

    public DecisionContainerGenerator moduleGenerator() {
        return moduleGenerator;
    }

    @Override
    public void updateConfig(ConfigGenerator cfg) {
        // nothing.
    }

    private void storeFile(GeneratedFile.Type type, String path, String source) {
        generatedFiles.add(new GeneratedFile(type, path, log(source).getBytes(StandardCharsets.UTF_8)));
    }

    public List<GeneratedFile> getGeneratedFiles() {
        return generatedFiles;
    }

    @Override
    public ApplicationSection section() {
        return moduleGenerator;
    }

    public DecisionCodegen withMonitoring(boolean useMonitoring) {
        this.useMonitoring = useMonitoring;
        return this;
    }

    public static DecisionCodegen ofJar(Path jarPath) throws IOException {
        List<DMNResource> resources = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(jarPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                ResourceType resourceType = determineResourceType(entry.getName());
                if (entry.getName().endsWith(".dmn")) {
                    InternalResource resource = new ByteArrayResource(readBytesFromInputStream(zipFile.getInputStream(entry)));
                    resource.setResourceType(resourceType);
                    resource.setSourcePath(entry.getName());
                    resources.add(toDmnResource(resource));
                }
            }
        }

        return ofDecisions(jarPath, resources);
    }

    public static DecisionCodegen ofPath(Path path) throws IOException {
        Path srcPath = Paths.get(path.toString());
        try (Stream<Path> filesStream = Files.walk(srcPath)) {
            List<File> files = filesStream.filter(p -> p.toString().endsWith(".dmn"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            return ofFiles(srcPath, files);
        }
    }

    public static DecisionCodegen ofFiles(Path basePath, Collection<File> files) throws IOException {
        List<DMNResource> result = parseDecisions(files);
        return ofDecisions(basePath, result);
    }

    public List<GeneratedFile> generate() {
        if (models.isEmpty()) {
            return Collections.emptyList();
        }

        List<DMNRestResourceGenerator> rgs = new ArrayList<>(); // REST resources

        for (DMNResource dmnRes : models.values()) {
            DMNRestResourceGenerator resourceGenerator = new DMNRestResourceGenerator(dmnRes.getDefinitions(), applicationCanonicalName)
                    .withDependencyInjection(annotator)
                    .withMonitoring(useMonitoring);
            rgs.add(resourceGenerator);
        }

        for (DMNRestResourceGenerator resourceGenerator : rgs) {
            if (useMonitoring) {
                generateAndStoreGrafanaDashboard(resourceGenerator);
            }

            storeFile(GeneratedFile.Type.REST, resourceGenerator.generatedFilePath(), resourceGenerator.generate());
        }

        return generatedFiles;
    }

    private void generateAndStoreGrafanaDashboard(DMNRestResourceGenerator resourceGenerator) {
        Definitions definitions = resourceGenerator.getDefinitions();
        List<TDecision> decisions = definitions.getDrgElement().stream().filter(x -> x.getParentDRDElement() instanceof TDecision).map(x -> (TDecision) x).collect(Collectors.toList());

        String dashboard = GrafanaConfigurationWriter.generateDashboardForDMNEndpoint(resourceGenerator.getNameURL(), decisions);
        generatedFiles.add(
                new org.kie.kogito.codegen.GeneratedFile(
                        org.kie.kogito.codegen.GeneratedFile.Type.RESOURCE,
                        "/dashboards/dashboard-endpoint-" + resourceGenerator.getNameURL() + ".json",
                        dashboard));
    }

    private static DecisionCodegen ofDecisions(Path basePath, List<DMNResource> result) {
        return new DecisionCodegen(basePath, result);
    }

    private static List<DMNResource> parseDecisions(Collection<File> files) throws IOException {
        List<DMNResource> result = new ArrayList<>();
        for (File dmnFile : files) {
            FileSystemResource r = new FileSystemResource(dmnFile);
            result.add(toDmnResource(r));
        }
        return result;
    }

    private static DMNResource toDmnResource(Resource r) throws IOException {
        Definitions defs = parseDecisionFile(r);
        return new DMNResource(new QName(defs.getNamespace(), defs.getName()), new ResourceWithConfigurationImpl(r, null, null, null), defs);
    }

    private static Definitions parseDecisionFile(Resource r) throws IOException {
        return DMNMarshallerFactory.newDefaultMarshaller().unmarshal(r.getReader());
    }
}