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
