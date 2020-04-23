package io.quarkus.it.kogito.drools.newunithotreloadtest;

import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitData;

/**
 * PersonUnit
 */
public class PersonUnit implements RuleUnitData {

    private DataStore<Person> persons = DataSource.createStore();

    public PersonUnit() {
    }

    public DataStore<Person> getPersons() {
        return persons;
    }

    public void setPersons(DataStore<Person> persons) {
        this.persons = persons;
    }

}