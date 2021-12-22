package org.kie.kogito.incubation.rules.data;

import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.common.LocalUri;
import org.kie.kogito.incubation.common.LocalUriId;

public class DataSourceId extends LocalUriId implements LocalId {
    public DataSourceId(LocalUri path) {
        super(path);
    }
}
