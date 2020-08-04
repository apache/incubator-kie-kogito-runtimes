/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.drools.core.impl;

import java.util.Map;

import org.drools.core.SessionConfiguration;
import org.drools.core.common.InternalAgenda;
import org.drools.core.spi.FactHandleFactory;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.process.ProcessInstance;

public class KogitoStatefulKnowledgeSessionImpl extends StatefulKnowledgeSessionImpl {

    public KogitoStatefulKnowledgeSessionImpl() {
    }

    public KogitoStatefulKnowledgeSessionImpl(long id, InternalKnowledgeBase kBase) {
        super(id, kBase);
    }

    public KogitoStatefulKnowledgeSessionImpl(long id, InternalKnowledgeBase kBase, boolean initInitFactHandle, SessionConfiguration config, Environment environment) {
        super(id, kBase, initInitFactHandle, config, environment);
    }

    public KogitoStatefulKnowledgeSessionImpl(long id, InternalKnowledgeBase kBase, FactHandleFactory handleFactory, long propagationContext, SessionConfiguration config, InternalAgenda agenda, Environment environment) {
        super(id, kBase, handleFactory, propagationContext, config, agenda, environment);
    }

    public ProcessInstance startProcessInstance(String processInstanceId) {
        return getProcessRuntime().startProcessInstance( processInstanceId );
    }
}
