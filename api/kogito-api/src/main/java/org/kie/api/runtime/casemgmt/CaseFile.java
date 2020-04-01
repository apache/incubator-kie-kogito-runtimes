/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.api.runtime.casemgmt;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.kie.api.task.model.Comment;
import org.kie.kogito.Model;

public class CaseFile<T extends Model> {

    private String id;
    private String definitionId;
    private Date start;
    private Date end;
    private Date reopen;
    private T data;
    private Map<String, CaseRoleInstance> userAssignments;
    private Map<String, CaseRoleInstance> groupAssignments;
    private Map<String, Collection<String>> accessRestrictions;
    private List<Comment> comments;

    //TODO: Add if necessary
//    Long parentInstanceId;
//    Long parentWorkItemId;
//    TaskModelFactory factory = TaskModelProvider.getFactory();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Date getReopen() {
        return reopen;
    }

    public void setReopen(Date reopen) {
        this.reopen = reopen;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, CaseRoleInstance> getUserAssignments() {
        return userAssignments;
    }

    public void setUserAssignments(Map<String, CaseRoleInstance> userAssignments) {
        this.userAssignments = userAssignments;
    }

    public Map<String, CaseRoleInstance> getGroupAssignments() {
        return groupAssignments;
    }

    public void setGroupAssignments(Map<String, CaseRoleInstance> groupAssignments) {
        this.groupAssignments = groupAssignments;
    }

    public Map<String, Collection<String>> getAccessRestrictions() {
        return accessRestrictions;
    }

    public void setAccessRestrictions(Map<String, Collection<String>> accessRestrictions) {
        this.accessRestrictions = accessRestrictions;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
