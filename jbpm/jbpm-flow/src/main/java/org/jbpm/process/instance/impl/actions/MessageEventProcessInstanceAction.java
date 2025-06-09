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
package org.jbpm.process.instance.impl.actions;

import org.kie.api.runtime.KieRuntime;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;

public class MessageEventProcessInstanceAction extends AbstractEventProcessInstanceAction {

    public MessageEventProcessInstanceAction(String eventType, String event) {
        super(eventType, event, DEFAULT_SCOPE);
    }

    public MessageEventProcessInstanceAction(String eventType, String event, String scope) {
        super(eventType, event, scope);
    }

    @Override
    protected void notifyEvent(KogitoProcessContext context, KogitoProcessInstance processInstance, KogitoNodeInstance nodeInstance, KieRuntime kieRuntime, String eventType, Object event) {
        context.getKogitoProcessRuntime().getProcessEventSupport().fireOnMessage(processInstance, context.getNodeInstance(), kieRuntime, eventType, event);
    }
}
