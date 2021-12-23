package org.kie.kogito.core.rules.incubation.quarkus.support;

import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class DataSourceProvider {
    @Produces
    <T> DataStore<T> makeDataSource() {
        return DataSource.createStore();
    }
}
