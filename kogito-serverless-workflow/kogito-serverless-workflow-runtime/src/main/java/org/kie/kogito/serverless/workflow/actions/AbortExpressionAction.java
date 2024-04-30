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

import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.workflow.instance.NodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;

public class AbortExpressionAction extends BaseExpressionAction {

    public AbortExpressionAction(String lang, String expr, String inputVar) {
        super(lang, expr, inputVar);
    }

    public void execute(KogitoProcessContext context) throws Exception {
        if (expr.isValid()) {
            String error = evaluate(context, String.class);
            if (error != null) {
                setError(context, error);
            }
        } else {
            setError(context, expr.toString());
        }
    }

    private void setError(KogitoProcessContext context, String error) {
        ProcessInstance pi = (ProcessInstance) context.getProcessInstance();
        pi.setErrorState((NodeInstance) context.getNodeInstance(), new AbortExpressionException(error));
        pi.setState(KogitoProcessInstance.STATE_ABORTED);
    }
}
