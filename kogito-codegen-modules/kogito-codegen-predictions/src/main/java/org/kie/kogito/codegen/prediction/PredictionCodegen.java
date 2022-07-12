/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.codegen.prediction;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.dmg.pmml.PMML;
import org.drools.codegen.common.GeneratedFile;
import org.drools.codegen.common.GeneratedFileType;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.efesto.common.api.io.IndexFile;
import org.kie.efesto.common.api.model.FRI;
import org.kie.efesto.common.api.model.GeneratedExecutableResource;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.CompilationManager;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.core.AbstractGenerator;
import org.kie.kogito.codegen.prediction.config.PredictionConfigGenerator;
import org.kie.kogito.pmml.openapi.api.PMMLOASResult;
import org.kie.kogito.pmml.openapi.factories.PMMLOASResultFactory;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.api.exceptions.KiePMMLException;
import org.kie.pmml.commons.model.KiePMMLFactoryModel;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.commons.model.KiePMMLModelFactory;
import org.kie.pmml.compiler.commons.utils.KiePMMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.stream.Collectors.toList;
import static org.kie.efesto.common.api.model.FRI.SLASH;
import static org.kie.efesto.runtimemanager.api.utils.GeneratedResourceUtils.getGeneratedExecutableResource;
import static org.kie.pmml.commons.Constants.PMML_STRING;
import static org.kie.pmml.commons.utils.KiePMMLModelUtils.getSanitizedClassName;
import static org.kie.pmml.evaluator.core.utils.PMMLRuntimeHelper.loadAllKiePMMLModelFactories;

public class PredictionCodegen extends AbstractGenerator {

    public static final String DMN_JPMML_CLASS = "org.kie.dmn.jpmml.DMNjPMMLInvocationEvaluator";
    public static final String GENERATOR_NAME = "predictions";
    private static final Logger LOGGER = LoggerFactory.getLogger(PredictionCodegen.class);
    private static final GeneratedFileType PMML_TYPE = GeneratedFileType.of("PMML", GeneratedFileType.Category.SOURCE);
    private static final CompilationManager compilationManager = org.kie.efesto.compilationmanager.api.utils.SPIUtils.getCompilationManager(true).get();
    private final List<PMMLResource> resources;

    public PredictionCodegen(KogitoBuildContext context, List<PMMLResource> resources) {
        super(context, GENERATOR_NAME, new PredictionConfigGenerator(context));
        this.resources = resources;
    }

    public static PredictionCodegen ofCollectedResources(KogitoBuildContext context,
            Collection<CollectedResource> resources) {
        if (context.hasClassAvailable(DMN_JPMML_CLASS)) {
            LOGGER.info("jpmml libraries available on classpath, skipping kogito-pmml parsing and compilation");
            return ofPredictions(context, Collections.emptyList());
        }
        List<PMMLResource> pmmlResources = resources.stream()
                .filter(r -> r.resource().getResourceType() == ResourceType.PMML)
                .flatMap(r -> parsePredictions(r.basePath(), Collections.singletonList(r.resource())).stream())
                .collect(toList());
        return ofPredictions(context, pmmlResources);
    }

    private static PredictionCodegen ofPredictions(KogitoBuildContext context, List<PMMLResource> resources) {
        return new PredictionCodegen(context, resources);
    }

    private static List<PMMLResource> parsePredictions(Path path, List<Resource> resources) {
        List<PMMLResource> toReturn = new ArrayList<>();
        resources.forEach(resource -> {
            List<KiePMMLModel> kiePMMLModels = getKiePMMLModels(resource);
            String modelPath = resource.getSourcePath();
            PMMLResource toAdd = new PMMLResource(kiePMMLModels, path, modelPath);
            toReturn.add(toAdd);
        });
        return toReturn;
    }

    private static List<KiePMMLModel> getKiePMMLModels(Resource resource) {
        File pmmlFile = new File(resource.getSourcePath());
        // @TODO gcardosi: all the following line are a workaround needed until DROOLS-7050 (with a "persistent" storage of already compiled classes)
        // is implemented. Without that, loadAllKiePMMLModelFactories would throw ClassNotFoundException because it read from IndexFile
        // classes that have been generated/compiled with a different classloader
        String fileNameNoSuffix = pmmlFile.getName().replace(".pmml", "");
        Collection<FRI> fries = new HashSet<>();
        try {
            PMML pmml = KiePMMLUtil.load(new FileInputStream(pmmlFile), pmmlFile.getName());
            pmml.getModels().forEach(pmmlModel -> {
                String basePath = fileNameNoSuffix + SLASH + getSanitizedClassName(pmmlModel.getModelName());
                FRI toAdd = new FRI(basePath, PMML_STRING);
                fries.add(toAdd);
            });
        } catch (Exception e) {
            LOGGER.error("failed to load PMMLModel from {}", pmmlFile);
        }

        EfestoResource<File> efestoFileResource = new EfestoFileResource(pmmlFile);
        KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader =
                new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
        Collection<IndexFile> indexFiles = compilationManager.processResource(memoryCompilerClassLoader, efestoFileResource);
        IndexFile pmmlIndexFile = indexFiles.stream().filter(indexFile -> indexFile.getModel().equals("pmml"))
                .findFirst()
                .orElseThrow(() -> new KiePMMLException("Failed to retrieve generated IndexFile: please check classpath for required  dependencies"));
        Collection<GeneratedExecutableResource> executableResources = fries.stream()
                .map(fri -> getGeneratedExecutableResource(fri, pmmlIndexFile))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        // TODO end workaround. When DROOLS-7050 will be merged, there won't be the need anymore to filter by "FRI"
        Collection<KiePMMLModelFactory> kiePMMLModelFactories = loadAllKiePMMLModelFactories(executableResources, memoryCompilerClassLoader);
        return kiePMMLModelFactories.stream()
                .flatMap(factory -> factory.getKiePMMLModels().stream())
                .collect(toList());
    }

    @Override
    public Optional<ApplicationSection> section() {
        return Optional.of(new PredictionModelsGenerator(context(), applicationCanonicalName(), resources));
    }

    @Override
    protected Collection<GeneratedFile> internalGenerate() {
        List<GeneratedFile> files = new ArrayList<>();
        for (PMMLResource resource : resources) {
            generateModelsFromResource(files, resource);
        }
        return files;
    }

    @Override
    public boolean isEmpty() {
        return resources.isEmpty();
    }

    @Override
    public int priority() {
        return 40;
    }

    private void generateModelsFromResource(List<GeneratedFile> files, PMMLResource resource) {
        for (KiePMMLModel model : resource.getKiePmmlModels()) {
            generateModel(files, model, resource.getModelPath());
        }
    }

    private void generateModel(List<GeneratedFile> files, KiePMMLModel model, String modelPath) {
        //        generateModelBaseFiles(files, model, modelPath);
        generateModelRESTFiles(files, model);
    }

    //    private void generateModelBaseFiles(List<GeneratedFile> files, KiePMMLModel model, String modelPath) {
    //        if (model.getName() == null || model.getName().isEmpty()) {
    //            String errorMessage = String.format("Model name should not be empty inside %s", modelPath);
    //            throw new IllegalArgumentException(errorMessage);
    //        }
    //        if (!(model instanceof HasSourcesMap)) {
    //            String errorMessage = String.format("Expecting HasSourcesMap instance, retrieved %s inside %s", model.getClass().getName(), modelPath);
    //            throw new IllegalStateException(errorMessage);
    //        }
    //
    //        Map<String, String> sourceMap = ((HasSourcesMap) model).getSourcesMap();
    //        for (Map.Entry<String, String> sourceMapEntry : sourceMap.entrySet()) {
    //            String path = sourceMapEntry.getKey().replace('.', File.separatorChar) + ".java";
    //            files.add(new GeneratedFile(PMML_TYPE, path, sourceMapEntry.getValue()));
    //        }
    //
    //        if (model instanceof HasNestedModels) {
    //            for (KiePMMLModel nestedModel : ((HasNestedModels) model).getNestedModels()) {
    //                generateModelBaseFiles(files, nestedModel, modelPath);
    //            }
    //        }
    //    }

    private void generateModelRESTFiles(List<GeneratedFile> files, KiePMMLModel model) {
        if (!context().hasRESTForGenerator(this) || (model instanceof KiePMMLFactoryModel)) {
            return;
        }

        PMMLRestResourceGenerator resourceGenerator = new PMMLRestResourceGenerator(context(), model, applicationCanonicalName());
        files.add(new GeneratedFile(REST_TYPE, resourceGenerator.generatedFilePath(), resourceGenerator.generate()));

        PMMLOASResult oasResult = PMMLOASResultFactory.getPMMLOASResult(model);
        try {
            String jsonContent = new ObjectMapper().writeValueAsString(oasResult.jsonSchemaNode());
            String jsonFile = String.format("%s.json", getSanitizedClassName(model.getName()));
            files.add(new GeneratedFile(GeneratedFileType.STATIC_HTTP_RESOURCE, jsonFile, jsonContent));
        } catch (Exception e) {
            LOGGER.warn("Failed to write OAS schema");
        }
    }
}
