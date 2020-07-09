/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.pmml;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.core.io.impl.ReaderResource;
import org.kie.api.io.Resource;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.kogito.Application;
import org.kie.pmml.commons.model.KiePMMLModel;
import org.kie.pmml.evaluator.api.executor.PMMLRuntime;
import org.kie.pmml.evaluator.core.PMMLContextImpl;
import org.kie.pmml.evaluator.core.executor.PMMLModelEvaluatorFinderImpl;
import org.kie.pmml.evaluator.core.utils.PMMLRequestDataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal Utility class.<br/>
 * Use {@link Application#predictionModels()} of Kogito API to programmatically access PMML assets and evaluate PMML predictions.
 */
public class PMMLKogito {

    private static final Logger LOG = LoggerFactory.getLogger(PMMLKogito.class);

    private PMMLKogito() {
        // intentionally private.
    }

    /**
     * Internal Utility class.<br/>
     * Use {@link Application#predictionModels()} of Kogito API to programmatically access PMML assets and evaluate PMML decisions.
     */
    public static PMMLRuntime createGenericPMMLRuntime(Reader... readers) {
        List<Resource> resources = Stream.of(readers).map(ReaderResource::new).collect(Collectors.toList());
        PMMLRuntime pmmlRuntime = PMMLRuntimeBuilder.fromResources(resources, new PMMLModelEvaluatorFinderImpl());
        return pmmlRuntime;
    }

    public static KiePMMLModel modelByName(PMMLRuntime pmmlRuntime, String modelName) {
        List<KiePMMLModel> modelsWithName = pmmlRuntime.getModels().stream().filter(m -> modelName.equals(m.getName())).collect(Collectors.toList());
        if (modelsWithName.size() == 1) {
            return modelsWithName.get(0);
        } else {
            throw new RuntimeException("Multiple model with the same name: " + modelName);
        }
    }

    public static PMML4Result evaluate(PMMLRuntime pmmlRuntime, String modelName, Map<String, Object> dmnContext) {
        final PMMLRequestData pmmlRequestData = getPMMLRequestData(modelName, dmnContext);
        return pmmlRuntime.evaluate(modelName, new PMMLContextImpl(pmmlRequestData));
    }


    private static PMMLRequestData getPMMLRequestData(String modelName, Map<String, Object> parameters) {
        String correlationId = "CORRELATION_ID";
        PMMLRequestDataBuilder pmmlRequestDataBuilder = new PMMLRequestDataBuilder(correlationId, modelName);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object pValue = entry.getValue();
            Class class1 = pValue.getClass();
            pmmlRequestDataBuilder.addParameter(entry.getKey(), pValue, class1);
        }
        return pmmlRequestDataBuilder.build();
    }

}
