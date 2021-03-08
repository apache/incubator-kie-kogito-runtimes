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
