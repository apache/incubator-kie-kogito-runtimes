/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kie.kogito.rules.RuleUnitInstance;

public class Executor {

    private final ExecutorService executorService;

    public static Executor create() {
        return new Executor();
    }

    private Executor() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void submit(RuleUnitInstance<?> instance) {
        this.executorService.submit(() -> {
            instance.fire();
        });
    }
}
