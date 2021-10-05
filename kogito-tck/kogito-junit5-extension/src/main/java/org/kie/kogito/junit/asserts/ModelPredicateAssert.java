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

import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Assertions;
import org.kie.kogito.Model;

public class ModelPredicateAssert<T extends Model> {

    private T model;

    public ModelPredicateAssert(T model) {
        this.model = model;
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectAssert<T> output(String name) {
        return new ObjectAssert<T>((T) model.toMap().get(name));
    }

    public ModelPredicateAssert<T> hasSize(int size) {
        if (model.toMap().size() != size) {
            Assertions.fail("model size does not match. Expecting " + size);
        }
        return this;
    }
}
