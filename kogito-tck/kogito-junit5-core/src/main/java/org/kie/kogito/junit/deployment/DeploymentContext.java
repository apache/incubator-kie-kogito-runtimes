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

package org.kie.kogito.junit.deployment;

import java.util.ServiceLoader;

import org.kie.kogito.junit.deployment.spi.DeploymentInstanceBuilder;

public class DeploymentContext {

    private Deployment deployment;
    private DeploymentInstance deploymentInstance;

    public DeploymentContext(Deployment deployment) {
        this.deployment = deployment;
    }

    public void init() {
        DeploymentInstanceBuilder builder = ServiceLoader.load(DeploymentInstanceBuilder.class, DeploymentContext.class.getClassLoader()).findFirst().orElseThrow();
        this.deploymentInstance = builder.build(deployment);
    }

    public DeploymentInstance get() {
        return deploymentInstance;
    }

    public void destroy() {
        this.deploymentInstance.destroy();
    }
}
