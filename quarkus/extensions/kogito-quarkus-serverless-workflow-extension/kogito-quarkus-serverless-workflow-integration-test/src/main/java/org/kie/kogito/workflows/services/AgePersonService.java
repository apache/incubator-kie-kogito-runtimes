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

    public AgePerson fullFrom(String name, int age, double income, long creationDate,
            String cardId, double discount, int count, Boolean enabled, long birthDate) {
        Date receivedCreation = new Date(creationDate);
        Date receivedBirth = new Date(birthDate);
        AgePerson agePerson = new AgePerson(name, age, income, receivedCreation);
        agePerson.setBasicDataPerson(new BasicDataPerson(cardId, discount, count, enabled, receivedBirth));
        return agePerson;
    }
}
