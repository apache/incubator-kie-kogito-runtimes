package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.rules.data.DataId;
import org.kie.kogito.incubation.rules.data.DataSourceId;
import org.kie.kogito.incubation.rules.services.DataSourceService;

public class DataStreamFactory {
    public static <T extends DataContext> DataStream<T> of(DataSourceService svc, DataSourceId localId, Class<T> type) {
        return new DataStream<>() {
            @Override
            public DataId append(T value) {
                return svc.add(localId, value);
            }
        };
    }

}
