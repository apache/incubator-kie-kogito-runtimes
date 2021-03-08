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

package org.kie.kogito.junit.asserts;

import org.kie.kogito.Model;
import org.kie.kogito.junit.listeners.FlowProcessEventListenerTracker;
import org.kie.kogito.process.ProcessInstance;

public final class ProcessAssertions {

    private ProcessAssertions() {
        // do nothing
    }

    public static <T extends ProcessInstance<? extends Model>> ProcessPredicateAssert<T> assertThat(T instance) {
        return new ProcessPredicateAssert<T>(instance);
    }

    public static <T extends Model> ModelPredicateAssert<T> assertThat(T model) {
        return new ModelPredicateAssert<T>(model);
    }

    public static TrackProcessPredicateAssert assertThat(FlowProcessEventListenerTracker tracker) {
        return new TrackProcessPredicateAssert(tracker);
    }

}
