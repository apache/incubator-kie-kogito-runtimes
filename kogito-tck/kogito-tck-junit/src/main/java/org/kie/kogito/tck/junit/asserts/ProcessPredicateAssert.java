/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.tck.junit.asserts;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;

public class ProcessPredicateAssert<T extends ProcessInstance<? extends Model>> {

    private T instance;

    public ProcessPredicateAssert(T instance) {
        this.instance = instance;
    }

    public void isCompleted() {
        if(this.instance.status() != ProcessInstance.STATE_COMPLETED) {
            Assertions.fail("Process instance " + instance.id() + " is not completed");
        }
    }

    public void isActive() {
        if(this.instance.status() != ProcessInstance.STATE_ACTIVE) {
            Assertions.fail("Process instance " + instance.id() + " is not active");
        }
    }

    public void isAborted() {
        if(this.instance.status() != ProcessInstance.STATE_ABORTED) {
            Assertions.fail("Process instance " + instance.id() + " is not aborted");
        }
    }

    public void isEnded() {
        List<Integer> finished = Arrays.asList(ProcessInstance.STATE_ABORTED, ProcessInstance.STATE_ACTIVE);
        if(!finished.contains(this.instance.status())) {
            Assertions.fail("Process instance " + instance.id() + " has no ended");
        }
    }

    public void isError() {
        if(this.instance.status() != ProcessInstance.STATE_ERROR) {
            Assertions.fail("Process instance " + instance.id() + " is not in error state");
        }
    }
}
