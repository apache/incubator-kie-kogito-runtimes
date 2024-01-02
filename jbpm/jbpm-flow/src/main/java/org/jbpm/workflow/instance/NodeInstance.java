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
package org.jbpm.workflow.instance;

import java.util.Map;

import org.jbpm.process.instance.ContextInstance;
import org.kie.api.definition.process.Node;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;

/**
 * Represents a node instance in a RuleFlow. This is the runtime counterpart
 * of a node, containing all runtime state. Node instance classes also
 * contain the logic on what to do when it is being triggered (start
 * executing) or completed (end of execution).
 * 
 */
public interface NodeInstance extends KogitoNodeInstance {

    void trigger(KogitoNodeInstance from, String type);

    void cancel();

    void cancel(CancelType type);

    Node getNode();

    ContextInstance resolveContextInstance(String contextId, Object param);

    int getLevel();

    void setDynamicParameters(Map<String, Object> dynamicParameters);

    int getSlaCompliance();

    String getSlaTimerId();

    default KogitoProcessInstance getKogitoProcessInstance() {
        return (KogitoProcessInstance) getProcessInstance();
    }

}
