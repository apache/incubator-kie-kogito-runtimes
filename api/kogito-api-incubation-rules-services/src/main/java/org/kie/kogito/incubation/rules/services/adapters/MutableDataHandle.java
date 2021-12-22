package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.rules.data.DataId;
import org.kie.kogito.incubation.rules.services.DataSourceService;

public class MutableDataHandle {
    public static MutableDataHandle of(DataSourceService svc, DataId id) {
        return new MutableDataHandle(svc, id);
    }

    DataSourceService svc;
    DataId id;
    DataContext ctx;

    public MutableDataHandle(DataSourceService svc, DataId id) {
        this.svc = svc;
        this.id = id;
    }

    public void update() {
        // notify update to svc
        svc.update(id, ctx);
    }

    public void remove() {

    }

    public DataId id() {
        return id;
    }
}
