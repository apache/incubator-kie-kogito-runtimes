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

package org.kie.kogito.dashboard.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kie.kogito.dashboard.CustomDashboardStorage;
import org.kie.kogito.dashboard.model.CustomDashboardFilter;
import org.kie.kogito.dashboard.model.CustomDashboardInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDashboardStorageImpl implements CustomDashboardStorage {

    private static final String CUSTOM_DASHBOARD_STORAGE_PATH = "/dashboards/";
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDashboardStorageImpl.class);

    private final Map<String, CustomDashboardInfo> customDashboardInfoMap = new HashMap<>();

    private URL classLoaderCustomDashboardUrl;
    private URL customDashStorageUrl;

    private Optional<String> storageUrl;

    public CustomDashboardStorageImpl() {
        start(Thread.currentThread().getContextClassLoader().getResource(CUSTOM_DASHBOARD_STORAGE_PATH));
    }

    public CustomDashboardStorageImpl(final URL classLoaderFormsUrl) {
        start(classLoaderFormsUrl);
    }

    private void start(final URL classLoaderFormsUrl) {
        start(classLoaderFormsUrl, getCustomDashboardStorageUrl(classLoaderFormsUrl));
    }

    public void setStorageUrl(Optional<String> storageUrl) {
        this.storageUrl = storageUrl;
    }

    private void start(final URL classLoaderCustomDashboardUrl, final URL customDashStorageUrl) {
        try {
            this.classLoaderCustomDashboardUrl = classLoaderCustomDashboardUrl;
            this.customDashStorageUrl = customDashStorageUrl;
        } catch (Exception ex) {
            LOGGER.warn("Couldn't properly initialize CustomDashboardStorageImpl");
        } finally {
            init();
        }
    }

    private URL getCustomDashboardStorageUrl(URL classLoaderCustomDashboardUrl) {
        if (classLoaderCustomDashboardUrl == null) {
            return null;
        }

        File customDashStorageeFolder = new File(storageUrl.orElse(classLoaderCustomDashboardUrl.getFile()));

        if (!customDashStorageeFolder.exists() || !customDashStorageeFolder.isDirectory()) {
            LOGGER.warn("Cannot initialize form storage folder in path '" + customDashStorageeFolder.getPath() + "'");
        }

        try {
            return customDashStorageeFolder.toURI().toURL();
        } catch (MalformedURLException ex) {
            LOGGER.warn("Cannot initialize form storage folder in path '" + customDashStorageeFolder.getPath() + "'", ex);
        }
        return null;
    }

    @Override
    public int getCustomDashboardFilesCount() {
        return customDashboardInfoMap.size();
    }

    @Override
    public Collection<CustomDashboardInfo> getCustomDashboardFiles(CustomDashboardFilter filter) {
        if (filter != null && !filter.getNames().isEmpty()) {
            return customDashboardInfoMap.entrySet().stream()
                    .filter(entry -> StringUtils.containsAnyIgnoreCase(entry.getKey(), filter.getNames().toArray(new String[0])))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        } else {
            return customDashboardInfoMap.values();
        }
    }

    @Override
    public String getCustomDashboardFileContent(String name) throws IOException {
        try {
            return IOUtils.toString(new FileInputStream(customDashboardInfoMap.get(name).getPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.info("custom-dashboard's file {} can not ready, because of {}", customDashboardInfoMap.get(name).getPath(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateCustomDashboard(String content) {

    }

    private void init() {
        readCustomDashboardResources().stream()
                .forEach(file -> {
                    LocalDateTime lastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), TimeZone.getDefault().toZoneId());
                    customDashboardInfoMap.put(file.getName(),
                            new CustomDashboardInfo(file.getName(), getRelativePath(file), lastModified));
                });
    }

    private Collection<File> readCustomDashboardResources() {
        if (classLoaderCustomDashboardUrl != null) {
            LOGGER.info("custom-dashboard's files path is {}", classLoaderCustomDashboardUrl.toString());
            File rootFolder = FileUtils.toFile(classLoaderCustomDashboardUrl);
            return FileUtils.listFiles(rootFolder, new String[] { "dash.yaml" }, false);
        }
        return Collections.emptyList();
    }

    private String getRelativePath(File file) {
        return classLoaderCustomDashboardUrl.getPath() + file.getName();
    }
}
