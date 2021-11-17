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
package org.kie.kogito.codegen.core.utils;

import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.api.AddonsConfig;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.AbstractKogitoBuildContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.codegen.core.utils.GrafanaDashboardUtils.DISABLED_DOMAIN_DASHBOARDS;
import static org.kie.kogito.codegen.core.utils.GrafanaDashboardUtils.DISABLED_OPERATIONAL_DASHBOARDS;

class GrafanaDashboardUtilsTest {

    protected KogitoBuildContext.Builder builder;

    @Test
    void isOperationDashboardEnabled() {
        builder = MockKogitoBuildContext.builder(DISABLED_OPERATIONAL_DASHBOARDS, null)
                .withAddonsConfig(AddonsConfig.DEFAULT);
        MockKogitoBuildContext context = (MockKogitoBuildContext) builder.build();
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(context, "Loan"));
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(context, "Hello"));

        builder = MockKogitoBuildContext.builder(DISABLED_OPERATIONAL_DASHBOARDS, "")
                .withAddonsConfig(AddonsConfig.DEFAULT);
        context = (MockKogitoBuildContext) builder.build();
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(context, "Loan"));
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(context, "Hello"));

        String values = "Hello";
        builder = MockKogitoBuildContext.builder(DISABLED_OPERATIONAL_DASHBOARDS, values)
                .withAddonsConfig(AddonsConfig.DEFAULT);
        context = (MockKogitoBuildContext) builder.build();
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(context, "Loan"));
        assertFalse(GrafanaDashboardUtils.isOperationDashboardEnabled(context, "Hello"));
        values = "Hello,Loan";

        builder = MockKogitoBuildContext.builder(DISABLED_OPERATIONAL_DASHBOARDS, values)
                .withAddonsConfig(AddonsConfig.DEFAULT);
        context = (MockKogitoBuildContext) builder.build();
        assertTrue(GrafanaDashboardUtils.isOperationDashboardEnabled(context, "Traffic"));
        assertFalse(GrafanaDashboardUtils.isOperationDashboardEnabled(context, "Loan"));
        assertFalse(GrafanaDashboardUtils.isOperationDashboardEnabled(context, "Hello"));
    }

    @Test
    void isDomainDashboardEnabled() {
        builder = MockKogitoBuildContext.builder(DISABLED_DOMAIN_DASHBOARDS, null)
                .withAddonsConfig(AddonsConfig.DEFAULT);
        MockKogitoBuildContext context = (MockKogitoBuildContext) builder.build();
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(context, "Loan"));
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(context, "Hello"));

        builder = MockKogitoBuildContext.builder(DISABLED_DOMAIN_DASHBOARDS, "")
                .withAddonsConfig(AddonsConfig.DEFAULT);
        context = (MockKogitoBuildContext) builder.build();
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(context, "Loan"));
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(context, "Hello"));

        String values = "Hello";
        builder = MockKogitoBuildContext.builder(DISABLED_DOMAIN_DASHBOARDS, values)
                .withAddonsConfig(AddonsConfig.DEFAULT);
        context = (MockKogitoBuildContext) builder.build();
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(context, "Loan"));
        assertFalse(GrafanaDashboardUtils.isDomainDashboardEnabled(context, "Hello"));
        values = "Hello,Loan";

        builder = MockKogitoBuildContext.builder(DISABLED_DOMAIN_DASHBOARDS, values)
                .withAddonsConfig(AddonsConfig.DEFAULT);
        context = (MockKogitoBuildContext) builder.build();
        assertTrue(GrafanaDashboardUtils.isDomainDashboardEnabled(context, "Traffic"));
        assertFalse(GrafanaDashboardUtils.isDomainDashboardEnabled(context, "Loan"));
        assertFalse(GrafanaDashboardUtils.isDomainDashboardEnabled(context, "Hello"));
    }

    static class MockKogitoBuildContext extends AbstractKogitoBuildContext {

        public static Builder builder(final String dashboardProperty, final String disabledDashboardsValue) {
            return new MockKogitoBuildContext.MockKogiotBuildContextBuilder(dashboardProperty, disabledDashboardsValue);
        }

        protected MockKogitoBuildContext(MockKogitoBuildContext.MockKogiotBuildContextBuilder builder) {
            super(builder, null, null, "Mock");
        }

        public static class MockKogiotBuildContextBuilder extends AbstractKogitoBuildContext.AbstractBuilder {

            protected MockKogiotBuildContextBuilder(final String dashboardProperty, final String disabledDashboardsValue) {
                if (disabledDashboardsValue != null) {
                    applicationProperties.setProperty(dashboardProperty, disabledDashboardsValue);
                }
            }

            @Override
            public KogitoBuildContext build() {
                return new MockKogitoBuildContext(this);
            }

        }
    }
}