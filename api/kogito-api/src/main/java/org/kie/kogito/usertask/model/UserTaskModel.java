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
package org.kie.kogito.usertask.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserTaskModel {

    private String taskName;
    private String taskDescription;
    private String taskPriority;
    private Set<String> potentialUsers;
    private Set<String> potentialGroups;
    private Set<String> adminUsers;
    private Set<String> adminGroups;
    private Set<String> excludedUsers;
    private List<Attachment> attachments;
    private List<Comment> comments;
    private String externalReferenceId;

    private Map<String, Object> inputs;
    private Map<String, Object> outputs;

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
    }

    /**
     * Returns name of the task
     * 
     * @return task name
     */
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Returns optional description of the task
     * 
     * @return task description if present
     */
    public String getTaskDescription() {
        return this.taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    /**
     * Returns optional priority of the task
     * 
     * @return task priority if present
     */
    public String getTaskPriority() {
        return this.taskPriority;
    }

    public void setTaskPriority(String taskPriority) {
        this.taskPriority = taskPriority;
    }

    /**
     * Returns potential users that can work on this task
     * 
     * @return potential users
     */
    public Set<String> getPotentialUsers() {
        return this.potentialUsers;
    }

    public void setPotentialUsers(Set<String> potentialUsers) {
        this.potentialUsers = potentialUsers;
    }

    /**
     * Returns potential groups that can work on this task
     * 
     * @return potential groups
     */
    public Set<String> getPotentialGroups() {
        return this.potentialGroups;
    }

    public void setPotentialGroups(Set<String> potentialGroups) {
        this.potentialGroups = potentialGroups;
    }

    /**
     * Returns admin users that can administer this task
     * 
     * @return admin users
     */
    public Set<String> getAdminUsers() {
        return this.adminUsers;
    }

    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }

    /**
     * Returns admin groups that can administer this task
     * 
     * @return admin groups
     */
    public Set<String> getAdminGroups() {
        return this.adminGroups;
    }

    /**
     * Returns excluded users that cannot work on this task
     * 
     * @return excluded users
     */
    public Set<String> getExcludedUsers() {
        return this.excludedUsers;
    }

    public void setExcludedUsers(Set<String> excludedUsers) {
        this.excludedUsers = excludedUsers;
    }

    /**
     * Returns task attachments
     * 
     * @return A map which key is the attachment id and value the attachment object
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Returns task comments
     * 
     * @return A map which key is the comment id and value the comment object
     */
    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }

}
