/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.serverless.workflow.parser.handlers;

import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.serverless.workflow.parser.NodeIdGenerator;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.states.DelayState;
import io.serverlessworkflow.api.states.EventState;
import io.serverlessworkflow.api.states.InjectState;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.states.ParallelState;
import io.serverlessworkflow.api.states.SubflowState;
import io.serverlessworkflow.api.states.SwitchState;

public class StateHandlerFactory {

    private StateHandlerFactory() {
    }

    public static StateHandler<?, ?, ?> getStateHandler(State state, Workflow workflow, RuleFlowProcessFactory factory, NodeIdGenerator idGenerator) {
        switch (state.getType()) {
            case EVENT:
                return new EventHandler((EventState) state, workflow, factory, idGenerator);
            case OPERATION:
                return new OperationHandler((OperationState) state, workflow, factory, idGenerator);
            case DELAY:
                return new DelayHandler((DelayState) state, workflow, factory, idGenerator);
            case INJECT:
                return new InjectHandler((InjectState) state, workflow, factory, idGenerator);
            case SUBFLOW:
                return new SubflowHandler((SubflowState) state, workflow, factory, idGenerator);
            case SWITCH:
                return new SwitchHandler((SwitchState) state, workflow, factory, idGenerator);
            case PARALLEL:
                return new ParallelHandler((ParallelState) state, workflow, factory, idGenerator);
            default:
                return null;
        }
    }

}
