/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.usertask.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kie.kogito.usertask.UserTask;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.UserTaskInstances;
import org.kie.kogito.usertask.impl.model.DeadlineHelper;
import org.kie.kogito.usertask.model.DeadlineInfo;
import org.kie.kogito.usertask.model.Reassignment;
import org.kie.kogito.usertask.model.UserTaskModel;

public class DefaultUserTask implements UserTask {

    private String separator = System.getProperty("org.jbpm.ht.user.separator", ",");

    private String id;
    private UserTaskInstances userTaskInstances;
    private String name;
    private String version;
    private String taskName;
    private String taskDescription;
    private String referenceName;
    private String taskPriority;
    private Boolean skippable;
    private Set<String> potentialUsers;
    private Set<String> potentialGroups;
    private Set<String> adminUsers;
    private Set<String> adminGroups;
    private Set<String> excludedUsers;
    private Collection<DeadlineInfo<Map<String, Object>>> startDeadlines;
    private Collection<DeadlineInfo<Map<String, Object>>> endDeadlines;
    private Collection<DeadlineInfo<Reassignment>> startReassigments;
    private Collection<DeadlineInfo<Reassignment>> endReassigments;

    public DefaultUserTask(String id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.userTaskInstances = new InMemoryUserTaskInstances();
    }

    @Override
    public UserTaskModel createModel() {
        UserTaskModel userTaskModel = new UserTaskModel();
        userTaskModel.setTaskName(getTaskName());
        userTaskModel.setTaskDescription(getTaskDescription());
        userTaskModel.setTaskPriority(getTaskPriority());
        userTaskModel.setPotentialUsers(getPotentialUsers());
        userTaskModel.setPotentialGroups(getPotentialGroups());
        userTaskModel.setAdminUsers(getAdminUsers());
        userTaskModel.setPotentialGroups(getPotentialGroups());
        userTaskModel.setExcludedUsers(getExcludedUsers());
        return userTaskModel;
    }

    @Override
    public UserTaskInstance createInstance(UserTaskModel userTaskModel) {
        UserTaskInstance instance = new DefaultUserTaskInstance(this.userTaskInstances, userTaskModel);
        userTaskInstances.create(instance);
        return instance;
    }

    @Override
    public UserTaskInstances instances() {
        return userTaskInstances;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String version() {
        return version;
    }

    public void setSkippable(String skippable) {
        this.skippable = Boolean.parseBoolean(skippable);
    }

    public Boolean getSkippable() {
        return skippable;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    @Override
    public String getTaskPriority() {
        return this.taskPriority;
    }

    public void setTaskPriority(String taskPriority) {
        this.taskPriority = taskPriority;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    @Override
    public String getReferenceName() {
        return referenceName;
    }

    @Override
    public Set<String> getPotentialUsers() {
        return this.potentialUsers;
    }

    public void setPotentialUsers(String potentialUsers) {
        this.setPotentialUsers(toSet(potentialUsers));
    }

    public void setPotentialUsers(Set<String> potentialUsers) {
        this.potentialUsers = potentialUsers;
    }

    @Override
    public Set<String> getPotentialGroups() {
        return this.potentialGroups;
    }

    public void setPotentialGroups(String potentialGroups) {
        this.setPotentialGroups(toSet(potentialGroups));
    }

    public void setPotentialGroups(Set<String> potentialGroups) {
        this.potentialGroups = potentialGroups;
    }

    @Override
    public Set<String> getAdminUsers() {
        return this.adminUsers;
    }

    public void setAdminUsers(String adminUsers) {
        this.setAdminUsers(toSet(adminUsers));
    }

    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }

    @Override
    public Set<String> getAdminGroups() {
        return this.adminGroups;
    }

    public void setAdminGroups(String adminGroups) {
        this.setAdminGroups(toSet(adminGroups));
    }

    public void setAdminGroups(Set<String> adminGroups) {
        this.adminGroups = adminGroups;
    }

    @Override
    public Set<String> getExcludedUsers() {
        return this.excludedUsers;
    }

    public void setExcludedUsers(String excludedUsers) {
        this.setExcludedUsers(toSet(excludedUsers));
    }

    public void setExcludedUsers(Set<String> excludedUsers) {
        this.excludedUsers = excludedUsers;
    }

    @Override
    public Collection<DeadlineInfo<Map<String, Object>>> getNotStartedDeadlines() {
        return startDeadlines;
    }

    public void setNotStartedDeadLines(String deadlines) {
        this.startDeadlines = DeadlineHelper.parseDeadlines(deadlines);
    }

    @Override
    public Collection<DeadlineInfo<Map<String, Object>>> getNotCompletedDeadlines() {
        return endDeadlines;
    }

    public void setNotCompletedDeadlines(String notStarted) {
        this.endDeadlines = DeadlineHelper.parseDeadlines(notStarted);
    }

    @Override
    public Collection<DeadlineInfo<Reassignment>> getNotStartedReassignments() {
        return startReassigments;
    }

    public void setNotStartedReassignments(String reassignments) {
        this.startReassigments = DeadlineHelper.parseReassignments(reassignments);
    }

    @Override
    public Collection<DeadlineInfo<Reassignment>> getNotCompletedReassigments() {
        return endReassigments;
    }

    public void setNotCompletedReassigments(String reassignments) {
        this.endReassigments = DeadlineHelper.parseReassignments(reassignments);
    }

    protected Set<String> toSet(String value) {
        Set<String> store = new HashSet<>();

        if (value != null) {
            for (String item : value.split(separator)) {
                store.add(item);
            }
        }
        return store;
    }
}
