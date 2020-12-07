/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.dmn;

import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.Application;
import org.kie.kogito.ExecutionIdSupplier;
import org.kie.kogito.decision.DecisionConfig;
import org.kie.kogito.decision.DecisionModels;

public abstract class AbstractDecisionModels implements DecisionModels {

    private final static boolean IS_NATIVE_IMAGE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    private DMNRuntime dmnRuntime;
    private ExecutionIdSupplier executionIdSupplier;
    private Application application;

    public org.kie.kogito.decision.DecisionModel getDecisionModel(java.lang.String namespace, java.lang.String name) {
        return new org.kie.kogito.dmn.DmnDecisionModel(getDmnRuntime(), namespace, name, getExecutionIdSupplier());
    }

    protected DMNRuntime getDmnRuntime() {
        return dmnRuntime;
    }

    protected void setDmnRuntime(DMNRuntime dmnRuntime) {
        if(getApplication() == null) {
            throw new IllegalStateException("Application should be provided before configuring DMNRuntime");
        }
        this.dmnRuntime = dmnRuntime;
        getApplication().config().get(DecisionConfig.class).decisionEventListeners().listeners().forEach(this.dmnRuntime::addListener);
    }

    protected ExecutionIdSupplier getExecutionIdSupplier() {
        return executionIdSupplier;
    }

    protected void setExecutionIdSupplier(ExecutionIdSupplier executionIdSupplier) {
        this.executionIdSupplier = executionIdSupplier;
    }

    protected Application getApplication() {
        return application;
    }

    protected void setApplication(Application application) {
        this.application = application;
    }

    protected static java.io.InputStreamReader readResource(java.io.InputStream stream) {
        if (!IS_NATIVE_IMAGE) {
            return new java.io.InputStreamReader(stream);
        }

        try {
            byte[] bytes = org.drools.core.util.IoUtils.readBytesFromInputStream(stream);
            java.io.ByteArrayInputStream byteArrayInputStream = new java.io.ByteArrayInputStream(bytes);
            return new java.io.InputStreamReader(byteArrayInputStream);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

}
