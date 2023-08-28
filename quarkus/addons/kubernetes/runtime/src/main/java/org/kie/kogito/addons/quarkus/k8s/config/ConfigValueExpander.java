/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addons.quarkus.k8s.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.smallrye.config.ConfigValue;

class ConfigValueExpander {

    private static final Pattern placeholderPattern = Pattern.compile("\\$\\{([^}]+)}");

    private final KubeDiscoveryConfigCache kubeDiscoveryConfigCache;

    ConfigValueExpander(KubeDiscoveryConfigCache kubeDiscoveryConfigCache) {
        this.kubeDiscoveryConfigCache = kubeDiscoveryConfigCache;
    }

    ConfigValue expand(ConfigValue configValue) {
        if (configValue != null && configValue.getRawValue() != null) {
            String serviceCoordinates = extractServiceCoordinates(configValue.getRawValue());
            if (serviceCoordinates != null) {
                return kubeDiscoveryConfigCache.get(configValue.getName(), serviceCoordinates)
                        .map(value -> interpolate(configValue.getRawValue(), value))
                        .map(configValue::withValue)
                        .orElse(configValue);
            }
        }

        return configValue;
    }

    public static String interpolate(String input, String replacement) {
        return placeholderPattern.matcher(input).replaceAll(replacement);
    }

    private static String extractServiceCoordinates(String rawValue) {
        Matcher matcher = placeholderPattern.matcher(rawValue);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
