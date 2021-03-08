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

package org.kogito.junit5.runtime.java;

import java.nio.file.Path;
import java.util.Map;

import org.kie.kogito.Application;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.junit.deployment.Deployment;
import org.kie.kogito.junit.deployment.DeploymentInstance;
import org.kie.kogito.junit.deployment.spi.impl.AbstractDeploymentInstanceBuilder;
import org.kie.kogito.process.Processes;

public class JavaDeploymentInstanceBuilder extends AbstractDeploymentInstanceBuilder {

    @SuppressWarnings("unchecked")
    @Override
    protected DeploymentInstance createDeploymentInstance(Deployment deployment, Path path) throws Exception {
        JavaFolderClassLoader classLoader = new JavaFolderClassLoader(JavaFolderClassLoader.class.getClassLoader(), path);

        JavaDeploymentInstance instance = new JavaDeploymentInstance(classLoader);

        instance.setApplication((Application) classLoader.loadClass(deployment.namespace() + ".Application").getConstructor().newInstance());

        getRuntimeTestPersistenceProvider().ifPresent(persistence -> persistence.prepare(instance.getApplication().get(Processes.class).processInstancesFactory()));

        for (Class<?> listener : deployment.getEventListeners()) {
            instance.register(listener.getConstructor().newInstance());
        }

        for (Map.Entry<String, Class<? extends KogitoWorkItemHandler>> entry : deployment.getWorkItemHandlers().entrySet()) {
            KogitoWorkItemHandler handler = entry.getValue().getConstructor().newInstance();
            String name = entry.getKey();
            instance.registerWorkItemHandler(name, handler);
        }

        return instance;
    }
}
