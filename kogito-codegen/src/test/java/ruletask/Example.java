package ruletask;

import java.util.concurrent.atomic.AtomicInteger;

import org.kie.kogito.codegen.data.Person;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStream;
import org.kie.kogito.rules.RuleUnitMemory;

public class Example implements RuleUnitMemory {

    String singleValue;
    DataStream<Person> persons = DataSource.createStream();
    private AtomicInteger counter = new AtomicInteger(0);

    public DataStream<Person> getPersons() {
        return persons;
    }

    public String getSingleValue() {
        return singleValue;
    }

    public void setSingleValue(String singleValue) {
        this.singleValue = singleValue;
    }

    public AtomicInteger getCounter() {
        return counter;
    }

}
