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
package org.kie.kogito.grafana.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class GrafanaDashboardUtils {

    public static final String DISABLED_OPERATIONAL_DASHBOARDS = "grafana.disabled.operational.dashboards";
    public static final String DISABLED_DOMAIN_DASHBOARDS = "grafana.disabled.domain.dashboards";

    private GrafanaDashboardUtils() {
    }

    public static boolean isOperationDashboardEnabled(final Properties applicationProperties, final String toVerify) {
        return isDashboardEnabled(applicationProperties, DISABLED_OPERATIONAL_DASHBOARDS, toVerify);
    }

    public static boolean isDomainDashboardEnabled(final Properties applicationProperties, final String toVerify) {
        return isDashboardEnabled(applicationProperties, DISABLED_DOMAIN_DASHBOARDS, toVerify);

    }

    static boolean isDashboardEnabled(final Properties applicationProperties, final String dashboardProperty, final String toVerify) {
        return Optional.ofNullable(applicationProperties.getProperty(dashboardProperty))
                .map(value -> !containsValue(value, toVerify)).orElse(true);
    }

    static boolean containsValue(String value, String toVerify) {
        List<String> items = Arrays.stream(value.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        return items.contains(toVerify);
    }
}
