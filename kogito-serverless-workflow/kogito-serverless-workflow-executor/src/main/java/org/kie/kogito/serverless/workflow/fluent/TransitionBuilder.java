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

import java.util.Deque;

import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.states.DefaultState;
import io.serverlessworkflow.api.transitions.Transition;

public class TransitionBuilder<T> {

    private final T container;
    private Deque<DefaultState> states;

    protected TransitionBuilder(T container, Deque<DefaultState> states) {
        this.container = container;
        this.states = states;
    }

    public TransitionBuilder<T> next(StateBuilder<?, ?> stateBuilder) {
        addTransition(stateBuilder.build());
        return this;
    }

    public T end(StateBuilder<?, ?> stateBuilder) {
        return end(stateBuilder, new End());
    }

    public T end(StateBuilder<?, ?> stateBuilder, End end) {
        addTransition(stateBuilder.build(end));
        return container;
    }

    private void addTransition(DefaultState state) {
        DefaultState prevState = states.getLast();
        prevState.setTransition(new Transition().withNextState(state.getName()));
        states.add(state);
    }

}
