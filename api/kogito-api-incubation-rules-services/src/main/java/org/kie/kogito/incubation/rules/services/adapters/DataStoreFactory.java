package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.rules.data.DataId;
import org.kie.kogito.incubation.rules.services.DataSourceService;

public class DataStoreFactory {
    public static <T extends DataContext> DataStore<T> of(DataSourceService svc, LocalId localId, Class<T> type) {
        return new DataStore<>() {
            @Override
            public MutableDataHandle add(T value) {
                return DataHandle.of(svc, svc.add(localId, value));
            }

            @Override
            public DataHandle remove(LocalId id) {
                return DataHandle.of(svc, svc.remove(id));
            }

        };
    }

}
