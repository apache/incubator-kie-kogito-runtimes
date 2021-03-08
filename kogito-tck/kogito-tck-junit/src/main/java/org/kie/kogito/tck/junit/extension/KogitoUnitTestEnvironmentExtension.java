/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.tck.junit.extension;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.kie.kogito.tck.junit.api.KogitoUnitTestEnvironment;
import org.kie.kogito.tck.junit.api.KogitoUnitTestEnvironmentProperty;

public class KogitoUnitTestEnvironmentExtension implements Extension, BeforeAllCallback, AfterAllCallback{

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Optional<Class<?>> testClass = context.getTestClass();
        if(!testClass.isPresent()) {
            return;
        }
        Class<?> clazz = testClass.get();
        KogitoUnitTestEnvironment env = clazz.getAnnotation(KogitoUnitTestEnvironment.class);
        if (env != null) {
            setEnvEntries(env);
        }

    }

    private void setEnvEntries (KogitoUnitTestEnvironment env) {
        for(KogitoUnitTestEnvironmentProperty property : env.entries()) {
            if(property.value() != null) {
                System.setProperty(property.name(), property.value());
            } else {
                System.clearProperty(property.name());
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        Optional<Class<?>> testClass = context.getTestClass();
        if(!testClass.isPresent()) {
            return;
        }
        Class<?> clazz = testClass.get();
        KogitoUnitTestEnvironment env = clazz.getAnnotation(KogitoUnitTestEnvironment.class);
        if(env != null) {
            clearEnvEntries(env);
        }
    }

    private void clearEnvEntries(KogitoUnitTestEnvironment env) {
        for(KogitoUnitTestEnvironmentProperty property : env.entries()) {
            System.clearProperty(property.name());
        }
    }



}
