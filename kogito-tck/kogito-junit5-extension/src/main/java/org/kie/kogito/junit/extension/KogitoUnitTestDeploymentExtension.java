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

package org.kie.kogito.junit.extension;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.junit.api.KogitoUnitTestContext;
import org.kie.kogito.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.junit.api.KogitoUnitTestDeploymentException;
import org.kie.kogito.junit.api.KogitoUnitTestListeners;
import org.kie.kogito.junit.api.KogitoUnitTestProcessDebug;
import org.kie.kogito.junit.api.KogitoUnitTestResource;
import org.kie.kogito.junit.api.KogitoUnitTestResourceType;
import org.kie.kogito.junit.api.KogitoUnitTestWorkItemHandler;
import org.kie.kogito.junit.api.KogitoUnitTestWorkItemHandlerRegistry;
import org.kie.kogito.junit.deployment.Deployment;
import org.kie.kogito.junit.deployment.DeploymentContext;
import org.kie.kogito.junit.deployment.DeploymentResource;
import org.kie.kogito.junit.listeners.DebugProcessEventListener;

public class KogitoUnitTestDeploymentExtension implements Extension, BeforeEachCallback, AfterEachCallback, BeforeAllCallback,
        AfterAllCallback, ParameterResolver {

    private Map<String, KogitoUnitTestContext> unitTestContexts;
    private Deployment defaultDeployment;

    public KogitoUnitTestDeploymentExtension() {
        unitTestContexts = new ConcurrentHashMap<>();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return KogitoUnitTestContext.class.isAssignableFrom(type) || unitTestContexts.get(extensionContext.getUniqueId()).isSupported(type) || Throwable.class.isAssignableFrom(type);

    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
            ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        if (KogitoUnitTestContext.class.isAssignableFrom(type)) {
            return unitTestContexts.get(extensionContext.getUniqueId());
        } else {
            return unitTestContexts.get(extensionContext.getUniqueId()).find(type);
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getTestClass().get();
        if (testClass.isAnnotationPresent(KogitoUnitTestDeployment.class)) {
            KogitoUnitTestDeployment archive = testClass.getAnnotation(KogitoUnitTestDeployment.class);
            defaultDeployment = toDeployment(archive);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        defaultDeployment = null;
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        unitTestContexts.remove(context.getUniqueId()).destroy();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        Method testMethod = context.getTestMethod().get();
        try {
            // template
            Deployment currentDeployment = defaultDeployment != null ? defaultDeployment.clone() : null;

            if (testMethod.isAnnotationPresent(KogitoUnitTestDeployment.class)) {
                // override default deployment with current deployment
                KogitoUnitTestDeployment archive = testMethod.getAnnotation(KogitoUnitTestDeployment.class);
                currentDeployment = toDeployment(archive);
            }

            if (currentDeployment == null) {
                throw new RuntimeException("No deployment specified !");
            }

            // time to add default listeners at class / test level
            Class<?> testClass = context.getTestClass().get();
            KogitoUnitTestListeners methodListeners = testMethod.getAnnotation(KogitoUnitTestListeners.class);
            KogitoUnitTestListeners classListeners = testClass.getAnnotation(KogitoUnitTestListeners.class);
            for (Class<?> listener : merge(classListeners, methodListeners)) {
                currentDeployment.addEventListener(listener);
            }
            KogitoUnitTestWorkItemHandlerRegistry methodWih = testMethod.getAnnotation(KogitoUnitTestWorkItemHandlerRegistry.class);
            KogitoUnitTestWorkItemHandlerRegistry classWith = testClass.getAnnotation(KogitoUnitTestWorkItemHandlerRegistry.class);
            for (Map.Entry<String, Class<? extends KogitoWorkItemHandler>> handler : merge(classWith, methodWih)) {
                currentDeployment.addWorkItemHandler(handler.getKey(), handler.getValue());
            }

            if (testMethod.isAnnotationPresent(KogitoUnitTestProcessDebug.class)) {
                currentDeployment.addEventListener(DebugProcessEventListener.class);
            }

            DeploymentContext deploymentContext = new DeploymentContext(currentDeployment);
            deploymentContext.init();
            KogitoUnitTestContextImpl unitTestContext = new KogitoUnitTestContextImpl(deploymentContext);
            unitTestContexts.put(context.getUniqueId(), unitTestContext);
        } catch (Throwable ex) {
            if (testMethod.isAnnotationPresent(KogitoUnitTestDeploymentException.class)) {
                unitTestContexts.put(context.getUniqueId(), new KogitoUnitTestErrorContextImpl(ex));
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    private List<Class<?>> merge(KogitoUnitTestListeners classListeners, KogitoUnitTestListeners methodListeners) {
        List<Class<?>> list = new ArrayList<>();
        if (classListeners != null) {
            list.addAll(Arrays.asList(classListeners.value()));
        }
        if (methodListeners != null) {
            list.addAll(Arrays.asList(methodListeners.value()));
        }
        return list;
    }

    private Set<Map.Entry<String, Class<? extends KogitoWorkItemHandler>>> merge(KogitoUnitTestWorkItemHandlerRegistry classWith, KogitoUnitTestWorkItemHandlerRegistry methodWih) {
        Map<String, Class<? extends KogitoWorkItemHandler>> registry = new HashMap<>();
        if (classWith != null) {
            for (KogitoUnitTestWorkItemHandler wih : classWith.entries()) {
                registry.put(wih.name(), wih.handler());
            }
        }
        if (methodWih != null) {
            for (KogitoUnitTestWorkItemHandler wih : methodWih.entries()) {
                registry.put(wih.name(), wih.handler());
            }
        }
        return registry.entrySet();
    }

    public Deployment toDeployment(KogitoUnitTestDeployment archive) {
        Deployment deployment = new Deployment(archive.namespace());

        // both listeners / wih have default values.
        for (Class<?> listener : archive.listeners()) {
            deployment.addEventListener(listener);
        }

        for (KogitoUnitTestWorkItemHandler handler : archive.workItemsHandlers()) {
            deployment.addWorkItemHandler(handler.name(), handler.handler());
        }

        toResources(archive).stream().forEach(e -> deployment.addResource(e));

        return deployment;
    }

    private List<DeploymentResource> toResources(KogitoUnitTestDeployment archive) {
        File file = new File(getClass().getClassLoader().getResource(".").getFile());
        String absolutePath = file.getAbsolutePath();

        List<DeploymentResource> resources = new ArrayList<>();
        for (KogitoUnitTestResource resource : archive.resources()) {
            KogitoUnitTestResourceType type = resource.type();
            Path resourcePath = Paths.get(absolutePath, resource.path());
            if (!Files.exists(resourcePath)) {
                throw new RuntimeException("File " + resource.path() + " does not exists!");
            }
            DeploymentResource.Type deploymentResourceType = null;
            switch (type) {
                case RULES:
                    deploymentResourceType = DeploymentResource.Type.RULES;
                    break;
                case DECISION:
                    deploymentResourceType = DeploymentResource.Type.DECISION;
                    break;
                case JAVA:
                    deploymentResourceType = DeploymentResource.Type.JAVA;
                    break;
                case PROCESS:
                    deploymentResourceType = DeploymentResource.Type.PROCESS;
                    break;
                case PREDICTION:
                    deploymentResourceType = DeploymentResource.Type.PREDICTION;
                    break;
            }
            resources.add(new DeploymentResource(resource.path(), deploymentResourceType));

        }
        return resources;
    }

}
