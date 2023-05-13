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

package tools.custom.dashboard.impl;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.kie.kogito.dashboard.impl.CustomDashboardStorageService;
import org.kie.kogito.dashboard.model.CustomDashboardFilter;
import org.kie.kogito.dashboard.model.CustomDashboardInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomDashboardStorageTest {

    private static String[] DASHBOARD_NAMES = { "age.dash.yaml", "products.dash.yaml" };
    private static String DASHBOARD_NAME = "age.dash.yaml";

    private CustomDashboardStorageService customDashboardStorage;

    @BeforeAll
    public void init() {
        URL tempFolder = Thread.currentThread().getContextClassLoader().getResource("custom/dashboard/");

        customDashboardStorage = new CustomDashboardStorageService(tempFolder, Optional.empty());
    }

    @Test
    public void testGetCustomDashboardFilesCount() {
        assertEquals(2, customDashboardStorage.getCustomDashboardFilesCount().get());
    }

    @Test
    public void testGetCustomDashboardFiles() {
        Collection<CustomDashboardInfo> customDashboardInfoFilterAll = customDashboardStorage.getCustomDashboardFiles(null).get();
        assertEquals(2, customDashboardInfoFilterAll.size());

        CustomDashboardFilter filterEmpty = new CustomDashboardFilter();
        filterEmpty.setNames(Collections.emptyList());
        Collection<CustomDashboardInfo> customDashboardInfoAllEmptyFilter = customDashboardStorage.getCustomDashboardFiles(filterEmpty).get();
        assertEquals(2, customDashboardInfoAllEmptyFilter.size());

        CustomDashboardFilter filter = new CustomDashboardFilter();
        filter.setNames(Arrays.asList(DASHBOARD_NAMES));

        Collection<CustomDashboardInfo> formInfos = customDashboardStorage.getCustomDashboardFiles(filter).get();
        assertEquals(2, formInfos.size());
    }

    @Test
    public void testGetCustomDashboardFileContent() throws IOException {
        String content = customDashboardStorage.getCustomDashboardFileContent(DASHBOARD_NAME).get();
        assertNotNull(content);
    }

    @Test
    public void testWrongStoragePath() throws IOException {
        CustomDashboardStorageService storageService = new CustomDashboardStorageService(Optional.of("Wrong-path"));
        try {
            storageService.getCustomDashboardFileContent("age.dash.yaml");
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
    }
}
