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
package org.kie.kogito.grafana.utils;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.grafana.utils.GrafanaDashboardUtils.DISABLED_DOMAIN_DASHBOARDS;
import static org.kie.kogito.grafana.utils.GrafanaDashboardUtils.DISABLED_OPERATIONAL_DASHBOARDS;

class GrafanaDashboardUtilsTest {

    @Test
    void isOperationDashboardEnabled() {
        Properties properties = new Properties();
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(properties, "Loan"));
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(properties, "Hello"));
        properties.setProperty(DISABLED_OPERATIONAL_DASHBOARDS, "");
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(properties, "Loan"));
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(properties, "Hello"));

        String values = "Hello";
        properties.setProperty(DISABLED_OPERATIONAL_DASHBOARDS, values);
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(properties, "Loan"));
        assertFalse(GrafanaDashboardUtils.isOperationDashboardEnabled(properties, "Hello"));

        values = "Hello,Loan";
        properties.setProperty(DISABLED_OPERATIONAL_DASHBOARDS, values);
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(properties, "Traffic"));
        assertFalse(GrafanaDashboardUtils.isOperationDashboardEnabled(properties, "Loan"));
        assertFalse(GrafanaDashboardUtils.isOperationDashboardEnabled(properties, "Hello"));
    }

    @Test
    void isDomainDashboardEnabled() {
        Properties properties = new Properties();

        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(properties, "Loan"));
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(properties, "Hello"));
        properties.setProperty(DISABLED_DOMAIN_DASHBOARDS, "");
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(properties, "Loan"));
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(properties, "Hello"));

        String values = "Hello";
        properties.setProperty(DISABLED_DOMAIN_DASHBOARDS, values);
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(properties, "Loan"));
        assertFalse(GrafanaDashboardUtils.isDomainDashboardEnabled(properties, "Hello"));

        values = "Hello,Loan";
        properties.setProperty(DISABLED_DOMAIN_DASHBOARDS, values);
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(properties, "Traffic"));
        assertFalse(GrafanaDashboardUtils.isDomainDashboardEnabled(properties, "Loan"));
        assertFalse(GrafanaDashboardUtils.isDomainDashboardEnabled(properties, "Hello"));
    }
}