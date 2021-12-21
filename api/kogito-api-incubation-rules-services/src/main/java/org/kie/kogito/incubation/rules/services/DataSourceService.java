package org.kie.kogito.incubation.rules.services;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.LocalId;

public interface DataSourceService {
    /**
     * @param id identifier of the data source
     * @param ctx data that should be inserted into the data source
     * @return FactHandleId
     */
    LocalId add(LocalId id, DataContext ctx);
}
