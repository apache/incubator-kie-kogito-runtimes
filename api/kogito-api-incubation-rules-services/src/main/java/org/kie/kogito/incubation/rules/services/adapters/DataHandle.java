package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.rules.data.DataId;
import org.kie.kogito.incubation.rules.services.DataSourceService;

public class DataHandle {
    public static DataHandle of(DataSourceService svc, DataId id) {
        return new DataHandle(svc, id);
    }

    DataSourceService svc;
    DataId id;
    DataContext ctx;

    public DataHandle(DataSourceService svc, DataId id) {
        this.svc = svc;
        this.id = id;
    }
}
