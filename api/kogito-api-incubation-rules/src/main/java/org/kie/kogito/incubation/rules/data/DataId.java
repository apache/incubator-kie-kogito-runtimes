package org.kie.kogito.incubation.rules.data;

import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.common.LocalUri;
import org.kie.kogito.incubation.common.LocalUriId;

public class DataId extends LocalUriId implements LocalId {
    public DataId(LocalUri path) {
        super(path);
    }

    public DataSourceId dataSourceId() {
        return null;
    }
}
