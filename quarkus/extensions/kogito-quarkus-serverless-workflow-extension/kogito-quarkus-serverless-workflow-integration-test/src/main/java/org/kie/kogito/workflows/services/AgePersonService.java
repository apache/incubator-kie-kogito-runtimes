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
package org.kie.kogito.workflows.services;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgePersonService {

    public AgePerson from(String name, int age, double income) {
        return new AgePerson(name, age, income);
    }

    public AgePerson createFrom(String name, int intValue, double income, long dateValue,
            String cardId, double discount, Boolean enabled) {
        Date receivedCreation = new Date(dateValue);
        Date receivedBirth = new Date(dateValue);
        AgePerson agePerson = new AgePerson(name, intValue, income, receivedCreation);
        agePerson.setBasicDataPerson(new BasicDataPerson(cardId, discount, intValue, enabled, receivedBirth));
        return agePerson;
    }
}
