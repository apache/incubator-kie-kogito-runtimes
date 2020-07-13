/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.marshalling.impl;

import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;

/**
 * Marshaller class for RuleFlowProcessInstances
 */

public class RuleFlowProcessInstanceMarshaller extends AbstractProcessInstanceMarshaller {

    public static RuleFlowProcessInstanceMarshaller INSTANCE = new RuleFlowProcessInstanceMarshaller();

    private RuleFlowProcessInstanceMarshaller() {
    }

    protected WorkflowProcessInstanceImpl createProcessInstance() {
        return new RuleFlowProcessInstance();
    }
}
