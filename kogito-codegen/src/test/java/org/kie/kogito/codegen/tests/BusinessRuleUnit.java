package org.kie.kogito.codegen.tests;

import org.kie.kogito.codegen.data.Person;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitMemory;

public class BusinessRuleUnit implements RuleUnitMemory {
    DataStore<Person> persons = DataSource.createStore();

    public DataStore<Person> getPersons() {
        return persons;
    }
}
