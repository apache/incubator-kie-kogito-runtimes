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

package org.kie.kogito.quarkus.common.deployment;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.eclipse.microprofile.config.ConfigProvider;
import org.kie.kogito.codegen.api.context.KogitoApplicationPropertyProvider;

import static java.util.stream.Collectors.toSet;

public class KogitoQuarkusApplicationPropertiesProvider implements KogitoApplicationPropertyProvider {

    @Override
    public Optional<String> getApplicationProperty(String property) {
        return ConfigProvider.getConfig().getOptionalValue(property, String.class);
    }

    @Override
    public Collection<String> getApplicationProperties() {
        return StreamSupport.stream(ConfigProvider.getConfig().getPropertyNames().spliterator(), false).collect(toSet());
    }

    @Override
    public <T> Optional<T> getApplicationProperty(String property, Class<T> clazz) {
        return ConfigProvider.getConfig().getOptionalValue(property, clazz);
    }

    @Override
    public void setApplicationProperty(String key, String value) {
        System.setProperty(key, value);
    }
}
