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
package org.jbpm.process.instance.impl.humantask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.kogito.process.workitems.impl.KogitoWorkItemImpl;
import org.kie.kogito.usertask.Attachment;
import org.kie.kogito.usertask.Comment;

public class HumanTaskWorkItemImpl extends KogitoWorkItemImpl implements InternalHumanTaskWorkItem {

    private static final long serialVersionUID = 6168927742199190604L;

    private String taskName;
    private String taskDescription;
    private String taskPriority;
    private String referenceName;

    private String actualOwner;
    private Set<String> potentialUsers = new HashSet<>();
    private Set<String> potentialGroups = new HashSet<>();
    private Set<String> excludedUsers = new HashSet<>();
    private Set<String> adminUsers = new HashSet<>();
    private Set<String> adminGroups = new HashSet<>();
    private Map<Object, Comment> comments = new ConcurrentHashMap<>();
    private Map<Object, Attachment> attachments = new ConcurrentHashMap<>();

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
        return taskPriority;
    }

    public void setTaskPriority(String taskPriority) {
        this.taskPriority = taskPriority;
    }

    @Override
    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    @Override
    public String getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
    }

    @Override
    public Set<String> getPotentialUsers() {
        return potentialUsers;
    }

    public void setPotentialUsers(Set<String> potentialUsers) {
        this.potentialUsers = potentialUsers;
    }

    @Override
    public Set<String> getPotentialGroups() {
        return potentialGroups;
    }

    public void setPotentialGroups(Set<String> potentialGroups) {
        this.potentialGroups = potentialGroups;
    }

    @Override
    public Set<String> getExcludedUsers() {
        return excludedUsers;
    }

    public void setExcludedUsers(Set<String> excludedUsers) {
        this.excludedUsers = excludedUsers;
    }

    @Override
    public Set<String> getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }

    @Override
    public Set<String> getAdminGroups() {
        return adminGroups;
    }

    public void setAdminGroups(Set<String> adminGroups) {
        this.adminGroups = adminGroups;
    }

    @Override
    public Map<Object, Attachment> getAttachments() {
        return attachments;
    }

    @Override
    public Map<Object, Comment> getComments() {
        return comments;
    }

    @Override
    public void setAttachment(String id, Attachment attachment) {
        attachments.put(id, attachment);
    }

    @Override
    public Attachment removeAttachment(String id) {
        return attachments.remove(id);
    }

    @Override
    public void setComment(String id, Comment comment) {
        comments.put(id, comment);
    }

    @Override
    public Comment removeComment(String id) {
        return comments.remove(1);
    }
}
