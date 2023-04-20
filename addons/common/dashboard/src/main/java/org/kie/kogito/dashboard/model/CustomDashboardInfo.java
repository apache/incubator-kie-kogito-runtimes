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

package org.kie.kogito.dashboard.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class CustomDashboardInfo {
    String name;
    String path;
    LocalDateTime lastUpdated;
    String content;
    String serverUrl;

    public CustomDashboardInfo(String name, String path, LocalDateTime lastUpdated, String content) {
        this.name = name;
        this.path = path;
        this.lastUpdated = lastUpdated;
        this.content = content;
    }

    public CustomDashboardInfo(String name, String path, LocalDateTime lastUpdated) {
        this.name = name;
        this.path = path;
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomDashboardInfo)) {
            return false;
        }

        CustomDashboardInfo that = (CustomDashboardInfo) o;

        if (!Objects.equals(name, that.name)) {
            return false;
        }
        if (!Objects.equals(path, that.path)) {
            return false;
        }
        if (!Objects.equals(lastUpdated, that.lastUpdated)) {
            return false;
        }
        if (!Objects.equals(content, that.content)) {
            return false;
        }
        return Objects.equals(serverUrl, that.serverUrl);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (serverUrl != null ? serverUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CustomDashboardInfo{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

}
