package org.kie.kogito.core.rules.incubation.quarkus.support;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.rules.data.DataId;
import org.kie.kogito.incubation.rules.services.DataSourceService;
import org.kie.kogito.rules.RuleUnits;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class QuarkusDataSourceDataSourceService implements DataSourceService {
    @Inject
    RuleUnits ruleUnits;
    DataSourceServiceImpl dataSourceService;

    @PostConstruct
    void setup() {
        dataSourceService = new DataSourceServiceImpl(ruleUnits);
    }

    @Override
    public DataContext get(DataId id) {
        return dataSourceService.get(id);
    }

    @Override
    public DataId add(LocalId dataSourceId, DataContext ctx) {
        return dataSourceService.add(dataSourceId, ctx);
    }

    @Override
    public void update(DataId dataId, DataContext ctx) {
        dataSourceService.update(dataId, ctx);
    }

    @Override
    public void remove(DataId dataId) {
        dataSourceService.remove(dataId);
    }
}
