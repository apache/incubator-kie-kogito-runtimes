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
package org.jbpm.process.instance.event;

import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.kie.api.event.usertask.UserTaskStateEvent;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.process.ProcessInstance;

public class UserTaskStateEventImpl extends UserTaskEventImpl implements UserTaskStateEvent {

    private static final long serialVersionUID = 4556236095420836309L;
    private String oldStatus;
    private String newStatus;

    public UserTaskStateEventImpl(ProcessInstance instance, HumanTaskNodeInstance nodeInstance, KieRuntime kruntime, String identity) {
        super(instance, nodeInstance, kruntime, identity);
    }

    @Override
    public String getUserTaskDefinitionId() {
        return getHumanTaskNodeInstance().getNodeDefinitionId();
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;

    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;

    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getOldStatus() {
        return oldStatus;
    }
}
