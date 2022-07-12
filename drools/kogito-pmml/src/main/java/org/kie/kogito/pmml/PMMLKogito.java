/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.pmml;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.efesto.compilationmanager.api.model.EfestoFileResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.CompilationManager;
import org.kie.memorycompiler.KieMemoryCompiler;
import org.kie.pmml.api.PMMLRuntimeFactory;
import org.kie.pmml.api.exceptions.KiePMMLException;
import org.kie.pmml.api.models.PMMLModel;
import org.kie.pmml.api.runtime.PMMLRuntime;
import org.kie.pmml.evaluator.core.PMMLContextImpl;

import static org.kie.kogito.pmml.utils.PMMLUtils.getPMMLRequestData;

/**
 * Internal Utility class
 */
public class PMMLKogito {

    private static final CompilationManager compilationManager = org.kie.efesto.compilationmanager.api.utils.SPIUtils.getCompilationManager(true).get();
    private static final PMMLRuntimeFactory PMML_RUNTIME_FACTORY = org.kie.pmml.evaluator.utils.SPIUtils.getPMMLRuntimeFactory(false);

    private PMMLKogito() {
        // intentionally private.
    }

    /**
     * Internal Utility class.<br/>
     *
     * @param pmmlPaths
     * @return
     */
    public static Map<String, PMMLRuntime> createPMMLRuntimes(String... pmmlPaths) {
        Map<String, PMMLRuntime> toReturn = new HashMap<>();
        // TODO gcardosi: can't work until DROOLS-7050
        Stream.of(pmmlPaths).forEach(pmmlPath -> toReturn.put(pmmlPath,
                getPMMLRuntimeAlreadyCompiled()));
        return toReturn;
    }

    public static PMMLModel modelByName(PMMLRuntime pmmlRuntime, String modelName) {
        List<PMMLModel> modelsWithName =
                pmmlRuntime.getPMMLModels().stream().filter(m -> modelName.equals(m.getName())).collect(Collectors.toList());
        if (modelsWithName.size() == 1) {
            return modelsWithName.get(0);
        } else {
            String errorMessage =
                    String.format("Wrong number of model(s) with name '%s': %s",
                            modelName,
                            modelsWithName.size());
            throw new KiePMMLException(errorMessage);
        }
    }

    public static PMML4Result evaluate(PMMLRuntime pmmlRuntime, String fileName, String modelName, Map<String, Object> pmmlContext) {
        final PMMLRequestData pmmlRequestData = getPMMLRequestData(modelName, pmmlContext);
        return pmmlRuntime.evaluate(modelName, new PMMLContextImpl(pmmlRequestData, fileName,
                pmmlRuntime.getMemoryClassLoader()));
    }

    private static PMMLRuntime getPMMLRuntimeAlreadyCompiled() {
        KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader =
                new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
        return PMML_RUNTIME_FACTORY.getPMMLRuntimeFromClassloader(memoryCompilerClassLoader);
    }

    private static PMMLRuntime getPMMLRuntimeWithInMemoryCompilation(File pmmlFile) {
        EfestoResource<File> darResource = new EfestoFileResource(pmmlFile);
        KieMemoryCompiler.MemoryCompilerClassLoader memoryCompilerClassLoader =
                new KieMemoryCompiler.MemoryCompilerClassLoader(Thread.currentThread().getContextClassLoader());
        compilationManager.processResource(memoryCompilerClassLoader, darResource);
        return PMML_RUNTIME_FACTORY.getPMMLRuntimeFromClassloader(memoryCompilerClassLoader);
    }

}
