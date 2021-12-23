package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.rules.data.DataSourceId;
import org.kie.kogito.incubation.rules.services.DataSourceService;

public class DataStoreFactory {
    public static <T extends DataContext> DataStore<T> of(DataSourceService svc, DataSourceId localId, Class<T> type) {
        return new DataStore<>() {
            @Override
            public DataHandle add(T value) {
                return DataHandle.of(svc, svc.add(localId, value));
            }

        };
    }

}
