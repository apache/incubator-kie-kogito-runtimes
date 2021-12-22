package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.rules.data.DataId;
import org.kie.kogito.incubation.rules.services.DataSourceService;

public class DataStoreFactory {
    public static <T extends DataContext> DataStore<T> of(DataSourceService svc, LocalId localId, Class<T> type) {
        return new DataStore<>() {
            @Override
            public DataId add(T value) {
                return svc.add(localId, value);
            }

            @Override
            public DataId remove(LocalId id) {
                return svc.remove(id);
            }

        };
    }

}
