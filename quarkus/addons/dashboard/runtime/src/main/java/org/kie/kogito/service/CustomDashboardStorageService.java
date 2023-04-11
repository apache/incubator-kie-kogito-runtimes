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

package org.kie.kogito.service;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.ConfigProvider;
import org.kie.kogito.dashboard.impl.CustomDashboardStorageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CustomDashboardStorageService extends CustomDashboardStorageImpl {
    public static final String PROJECT_CUSTOM_DASHBOARD_STORAGE_PROP = "quarkus.kogito-runtime-tools.custom.dashboard.folder";
    private static final String CUSTOM_DASHBOARD_STORAGE_PATH = "/dashboards/";
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDashboardStorageService.class);

    @PostConstruct
    public void init() {
        Optional<String> storageUrl = ConfigProvider.getConfig()
                .getOptionalValue(PROJECT_CUSTOM_DASHBOARD_STORAGE_PROP, String.class);
        this.setStorageUrl(storageUrl);
    }

}
