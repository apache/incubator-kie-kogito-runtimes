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
package org.kie.kogito.quarkus.drools;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitData;

public class AnotherService implements RuleUnitData, DataContext {
    DataStore<StringHolder> strings = DataSource.createStore();
    DataStore<StringHolder> greetings = DataSource.createStore();

    public DataStore<StringHolder> getStrings() {
        return strings;
    }

    public DataStore<StringHolder> getGreetings() {
        return greetings;
    }

    @Override
    public <T extends DataContext> T as(Class<T> type) {
        throw new UnsupportedOperationException();
    }
}
