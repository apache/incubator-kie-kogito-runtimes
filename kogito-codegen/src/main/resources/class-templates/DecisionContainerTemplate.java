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
public class DecisionModels implements org.kie.kogito.decision.DecisionModels {

    private final static boolean IS_NATIVE_IMAGE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    private final static java.util.function.Function<java.lang.String, org.kie.api.runtime.KieRuntimeFactory> kieRuntimeFactoryFunction = PredictionModels.kieRuntimeFactoryFunction;
    private final static org.kie.dmn.api.core.DMNRuntime dmnRuntime = org.kie.kogito.dmn.DMNKogito.createGenericDMNRuntime(kieRuntimeFactoryFunction);
    private final static org.kie.kogito.ExecutionIdSupplier execIdSupplier = null;

    public DecisionModels(org.kie.kogito.Application app) {
        app.config().decision().decisionEventListeners().listeners().forEach(dmnRuntime::addListener);
    }

    public org.kie.kogito.decision.DecisionModel getDecisionModel(java.lang.String namespace, java.lang.String name) {
        return new org.kie.kogito.dmn.DmnDecisionModel(dmnRuntime, namespace, name, execIdSupplier);
    }

    private static java.io.InputStreamReader readResource(java.io.InputStream stream) {
        if (!IS_NATIVE_IMAGE) {
            return new java.io.InputStreamReader(stream);
        }

        try {
            byte[] bytes = stream.readAllBytes();
            java.io.ByteArrayInputStream byteArrayInputStream = new java.io.ByteArrayInputStream(bytes);
            return new java.io.InputStreamReader(byteArrayInputStream);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

}
