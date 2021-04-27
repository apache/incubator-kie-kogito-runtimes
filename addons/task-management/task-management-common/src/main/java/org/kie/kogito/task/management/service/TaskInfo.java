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
package org.kie.kogito.task.management.service;

import java.util.Map;
import java.util.Set;

public class TaskInfo {

    private String description;
    private String priority;
    private Set<String> potentialUsers;
    private Set<String> potentialGroups;
    private Set<String> excludedUsers;
    private Set<String> adminUsers;
    private Set<String> adminGroups;
    private Map<String, Object> inputParams;

    public TaskInfo() {
    }

    public TaskInfo(String description, String priority, Set<String> potentialUsers, Set<String> potentialGroups,
            Set<String> excludedUsers, Set<String> adminUsers, Set<String> adminGroups,
            Map<String, Object> inputParams) {
        this.description = description;
        this.priority = priority;
        this.potentialUsers = potentialUsers;
        this.potentialGroups = potentialGroups;
        this.excludedUsers = excludedUsers;
        this.adminUsers = adminUsers;
        this.adminGroups = adminGroups;
        this.inputParams = inputParams;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Set<String> getPotentialUsers() {
        return potentialUsers;
    }

    public void setPotentialUsers(Set<String> potentialUsers) {
        this.potentialUsers = potentialUsers;
    }

    public Set<String> getPotentialGroups() {
        return potentialGroups;
    }

    public void setPotentialGroups(Set<String> potentialGroups) {
        this.potentialGroups = potentialGroups;
    }

    public Set<String> getExcludedUsers() {
        return excludedUsers;
    }

    public void setExcludedUsers(Set<String> excludedUsers) {
        this.excludedUsers = excludedUsers;
    }

    public Set<String> getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }

    public Set<String> getAdminGroups() {
        return adminGroups;
    }

    public void setAdminGroups(Set<String> adminGroups) {
        this.adminGroups = adminGroups;
    }

    public Map<String, Object> getInputParams() {
        return inputParams;
    }

    public void setInputParams(Map<String, Object> inputParams) {
        this.inputParams = inputParams;
    }

    @Override
    public String toString() {
        return "TaskInfo [description=" + description + ", priority=" + priority + ", potentialUsers=" +
                potentialUsers + ", potentialGroups=" + potentialGroups + ", excludedUsers=" + excludedUsers +
                ", adminUsers=" + adminUsers + ", adminGroups=" + adminGroups + ", inputParams=" + inputParams + "]";
    }
}
