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


package org.kie.kogito.dashboard;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.dashboard.impl.CustomDashboardStorageService;
import org.kie.kogito.dashboard.model.CustomDashboardFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CustomDashboardServiceTest {
    private CustomDashboardService customDashboardServiceTest;
    private CustomDashboardStorageService customDashboardStorageService;

    @BeforeEach
    void setup() {
        customDashboardServiceTest = new CustomDashboardService();
        customDashboardStorageService = mock(CustomDashboardStorageService.class);
        customDashboardServiceTest.setStorage(customDashboardStorageService);
    }

    @Test
    public void testGetCustomDashboardFilesCount(){
        customDashboardServiceTest.getCustomDashboardFilesCount();
        verify(customDashboardStorageService).getCustomDashboardFilesCount();
    }

    @Test
    public void testGetCustomDashboardFiles() {
        CustomDashboardFilter filter = new CustomDashboardFilter();
        customDashboardServiceTest.getCustomDashboardFiles(filter, anyString());
        verify(customDashboardStorageService).getCustomDashboardFiles(filter);
    }

    @Test
    public void testGetCustomDashboardFileContent() throws IOException {
        customDashboardServiceTest.getCustomDashboardFileContent(anyString());
        verify(customDashboardStorageService).getCustomDashboardFileContent(anyString());
    }
}
