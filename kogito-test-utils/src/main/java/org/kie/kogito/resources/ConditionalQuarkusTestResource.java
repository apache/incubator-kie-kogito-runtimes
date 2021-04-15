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
package org.kie.kogito.resources;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Quarkus resource to be run if and only if it was enabled.
 */
public abstract class ConditionalQuarkusTestResource<T extends TestResource> implements QuarkusTestResourceLifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionalQuarkusTestResource.class);

    private final T testResource;
    private final ConditionHolder condition;
    private boolean conditionalEnabled = false;

    public ConditionalQuarkusTestResource(T testResource) {
        this(testResource, new ConditionHolder(testResource.getResourceName()));
    }

    public ConditionalQuarkusTestResource(T testResource, ConditionHolder condition) {
        LOGGER.info("Create Test resource of class {}", testResource.getClass());
        this.testResource = testResource;
        this.condition = condition;
    }

    public T getTestResource() {
        return testResource;
    }

    public boolean isConditionalEnabled() {
        return conditionalEnabled;
    }

    @Override
    public Map<String, String> start() {
        if (condition.isEnabled()) {
            LOGGER.info("Start Test resource of class {}", testResource.getClass());
            testResource.start();
            return Collections.singletonMap(getKogitoProperty(), getKogitoPropertyValue());
        }

        LOGGER.info("Ignore Test resource of class {}", testResource.getClass());
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        if (condition.isEnabled()) {
            testResource.stop();
        }
    }

    @Override
    public void inject(Object testInstance) {
        Class<?> c = testInstance.getClass();
        while (c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                ConfigProperty configProperty = f.getAnnotation(ConfigProperty.class);
                if (configProperty != null && getKogitoProperty().equals(configProperty.name())) {
                    setFieldValue(f, testInstance, getKogitoPropertyValue());
                } else if (f.isAnnotationPresent(Resource.class) && f.getType().isInstance(this)) {
                    setFieldValue(f, testInstance, this);
                }
            }
            c = c.getSuperclass();
        }
    }

    protected abstract String getKogitoProperty();

    protected String getKogitoPropertyValue() {
        return "localhost:" + testResource.getMappedPort();
    }

    protected void enableConditional() {
        condition.enableConditional();
        conditionalEnabled = true;
    }

    private void setFieldValue(Field f, Object testInstance, Object value) {
        try {
            f.setAccessible(true);
            f.set(testInstance, value);
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
