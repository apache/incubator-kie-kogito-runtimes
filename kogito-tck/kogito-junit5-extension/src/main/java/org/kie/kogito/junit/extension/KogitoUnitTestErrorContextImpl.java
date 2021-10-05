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

package org.kie.kogito.junit.extension;

import java.util.Optional;

import org.kie.kogito.Model;
import org.kie.kogito.junit.api.KogitoUnitTestContext;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;

public class KogitoUnitTestErrorContextImpl implements KogitoUnitTestContext {

    private Throwable throwable;

    public KogitoUnitTestErrorContextImpl(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public <T> T find(Class<T> clazz) {
        if (Throwable.class.isAssignableFrom(clazz)) {
            return clazz.cast(throwable);
        }
        return null;
    }

    @Override
    public Process<? extends Model> processById(String processId) {
        return null;
    }

    @Override
    public Optional<ProcessInstance<? extends Model>> processInstanceById(String processId, String id) {
        return Optional.empty();
    }

    @Override
    public Optional<ProcessInstance<? extends Model>> findByBusinessKey(String processId, String businessKey) {
        return Optional.empty();
    }

    @Override
    public void destroy() {
        // nothing
    }

}
