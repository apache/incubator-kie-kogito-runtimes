/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.serverless.workflow.fluent;

import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.states.DefaultState;

public abstract class StateBuilder<T extends StateBuilder<T, S>, S extends DefaultState> {

    private static int counter;

    public static InjectStateBuilder inject(JsonNode data) {
        return new InjectStateBuilder(data);
    }

    public static OperationStateBuilder operation() {
        return new OperationStateBuilder();
    }

    protected final S state;

    protected StateBuilder(S state) {
        this.state = state;
    }

    public T name(String name) {
        state.withName(name);
        return (T) this;
    }

    public State build() {
        ensureName();
        return state;
    }

    public State build(End end) {
        ensureName();
        return state.withEnd(end);
    }

    private void ensureName() {
        if (state.getName() == null) {
            state.setName(state.getType() + "_" + counter++);
        }
    }

}
