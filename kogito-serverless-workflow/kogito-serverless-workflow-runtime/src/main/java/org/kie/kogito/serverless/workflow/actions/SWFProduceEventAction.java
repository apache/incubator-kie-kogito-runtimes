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
package org.kie.kogito.serverless.workflow.actions;

import java.util.function.Supplier;

import org.jbpm.process.instance.impl.actions.AbstractEventProcessInstanceAction;
import org.jbpm.ruleflow.core.Metadata;
import org.kie.api.runtime.KieRuntime;
import org.kie.kogito.event.impl.MessageProducerWithContext;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.jackson.utils.JsonNodeVisitor;
import org.kie.kogito.jackson.utils.JsonObjectUtils;
import org.kie.kogito.serverless.workflow.utils.ExpressionHandlerUtils;

import com.fasterxml.jackson.databind.JsonNode;

public class SWFProduceEventAction extends AbstractEventProcessInstanceAction {

    protected final String exprLang;
    protected final JsonNode data;
    protected final JsonNode contextAttrs;

    public SWFProduceEventAction(String triggerName, String varName, Supplier<MessageProducerWithContext<JsonNode>> supplier, String exprLang, JsonNode data, JsonNode contextAttrs) {
        super(triggerName, varName, Metadata.EXTERNAL_SCOPE);

        this.exprLang = exprLang;
        this.data = data;
        this.contextAttrs = contextAttrs;
    }

    protected Object transform(KogitoProcessContext context, Object object) {
        if (data != null) {
            return JsonNodeVisitor.transformTextNode(data, node -> ExpressionHandlerUtils.transform(node, object, context, exprLang));
        } else {
            return JsonObjectUtils.fromValue(object);
        }
    }

    @Override
    protected void notifyEvent(KogitoProcessContext context, KogitoProcessInstance processInstance, KogitoNodeInstance nodeInstance, KieRuntime kieRuntime, String eventType, Object event) {
        context.getKogitoProcessRuntime().getProcessEventSupport().fireOnMessage(processInstance, context.getNodeInstance(), kieRuntime, eventType, event);
    }
}
