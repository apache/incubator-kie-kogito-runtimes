package acme;

import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitData;

public class Hello implements RuleUnitData {
    DataStore<String> strings = DataSource.createStore();
    DataStore<Message> messages = DataSource.createStore();

    public DataStore<String> getStrings() {
        return strings;
    }
    
    public DataStore<Message> getMessages() {
        return messages;
    }
}