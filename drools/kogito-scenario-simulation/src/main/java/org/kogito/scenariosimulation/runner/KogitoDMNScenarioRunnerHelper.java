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
package org.kogito.scenariosimulation.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.drools.core.io.impl.FileSystemResource;
import org.drools.scenariosimulation.api.model.ScenarioSimulationModel;
import org.drools.scenariosimulation.api.model.ScesimModelDescriptor;
import org.drools.scenariosimulation.api.model.Settings;
import org.drools.scenariosimulation.backend.expression.ExpressionEvaluatorFactory;
import org.drools.scenariosimulation.backend.runner.DMNScenarioRunnerHelper;
import org.drools.scenariosimulation.backend.runner.ScenarioException;
import org.drools.scenariosimulation.backend.runner.model.ScenarioRunnerData;
import org.drools.scenariosimulation.backend.util.DMNSimulationUtils;
import org.kie.api.KieBase;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.kogito.pmml.PMMLKogito;
import org.kie.pmml.evaluator.core.utils.KnowledgeBaseUtils;

import static java.util.stream.Collectors.toList;
import static org.drools.scenariosimulation.backend.fluent.DMNScenarioExecutableBuilder.DMN_MODEL;
import static org.drools.scenariosimulation.backend.fluent.DMNScenarioExecutableBuilder.DMN_RESULT;

public class KogitoDMNScenarioRunnerHelper extends DMNScenarioRunnerHelper {

    private DMNRuntime dmnRuntime = initDmnRuntime();

    @Override
    protected Map<String, Object> executeScenario(KieContainer kieContainer,
            ScenarioRunnerData scenarioRunnerData,
            ExpressionEvaluatorFactory expressionEvaluatorFactory,
            ScesimModelDescriptor scesimModelDescriptor,
            Settings settings) {
        if (!ScenarioSimulationModel.Type.DMN.equals(settings.getType())) {
            throw new ScenarioException("Impossible to run a not-DMN simulation with DMN runner");
        }
        DMNModel dmnModel = getDMNModel(dmnRuntime, settings);
        DMNContext dmnContext = dmnRuntime.newContext();

        defineInputValues(scenarioRunnerData.getBackgrounds(), scenarioRunnerData.getGivens()).forEach(dmnContext::set);

        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);

        Map<String, Object> toReturn = new HashMap<>();
        toReturn.put(DMN_MODEL, dmnModel);
        toReturn.put(DMN_RESULT, dmnResult);

        return toReturn;
    }

    private DMNModel getDMNModel(DMNRuntime dmnRuntime, Settings settings) {
        try {
            return DMNSimulationUtils.extractDMNModel(dmnRuntime, settings.getDmnFilePath());
        } catch (Exception e) {
            // if filename is not available or it fails, try directly with namespace/name
            return dmnRuntime.getModel(settings.getDmnNamespace(), settings.getDmnName());
        }
    }

    private Function<String, KieRuntimeFactory> initPmmlKieRuntimeFactory() {
        try (Stream<Path> fileStream = Files.walk(Paths.get("."))) {
            Map<KieBase, KieRuntimeFactory> kieRuntimeFactories =
                    PMMLKogito.createKieRuntimeFactoriesWithInMemoryCompilation(
                            fileStream
                                    .filter(path -> filterResource(path, ".pmml"))
                                    .map(Path::toString)
                                    .toArray(String[]::new));

            return s -> kieRuntimeFactories.keySet().stream()
                    .filter(kieBase -> KnowledgeBaseUtils.getModel(kieBase, s).isPresent())
                    .map(kieRuntimeFactories::get)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Failed to fine KieRuntimeFactory for model " + s));

        } catch (IOException e) {
            throw new IllegalStateException("Error initializing KogitoDMNScenarioRunnerHelper", e);
        }
    }

    private DMNRuntime initDmnRuntime() {
        Function<String, KieRuntimeFactory> kieRuntimeFactoryFunction = initPmmlKieRuntimeFactory();

        try (Stream<Path> fileStream = Files.walk(Paths.get("."))) {
            List<Resource> resources = fileStream.filter(path -> filterResource(path, ".dmn"))
                    .map(Path::toFile)
                    .map(FileSystemResource::new)
                    .collect(toList());

            return DMNRuntimeBuilder.fromDefaults()
                    .setKieRuntimeFactoryFunction(kieRuntimeFactoryFunction)
                    .buildConfiguration()
                    .fromResources(resources)
                    .getOrElseThrow(e -> new RuntimeException("Error initializing DMNRuntime", e));
        } catch (IOException e) {
            throw new IllegalStateException("Error initializing KogitoDMNScenarioRunnerHelper", e);
        }
    }

    private boolean filterResource(Path path, String extension) {
        return path.toString().endsWith(extension) && !path.toString().contains("/target/") && Files.isRegularFile(path);
    }

}
