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

package org.kie.kogito.test.engine.domain;

import org.kie.kogito.conf.Clock;
import org.kie.kogito.conf.ClockType;
import org.kie.kogito.conf.SessionsPool;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitData;

@SessionsPool(1)
@Clock(ClockType.PSEUDO)
public class PersonUnit implements RuleUnitData {

    private DataStore<Person> persons;

    public PersonUnit() {
        this(DataSource.createStore());
    }

    public PersonUnit(DataStore<Person> persons) {
        this.persons = persons;
    }

    public PersonUnit(DataStore<Person> persons, int adultAge) {
        this.persons = persons;

    }

    public DataStore<Person> getPersons() {
        return persons;
    }

    @Override
    public String toString() {
        return "PersonUnit()";
    }
}
