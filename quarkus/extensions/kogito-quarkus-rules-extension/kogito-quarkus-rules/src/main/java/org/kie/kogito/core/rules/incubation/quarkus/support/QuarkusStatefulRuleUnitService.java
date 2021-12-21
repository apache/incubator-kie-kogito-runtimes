package org.kie.kogito.core.rules.incubation.quarkus.support;

import org.kie.kogito.incubation.common.ExtendedDataContext;
import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.common.MetaDataContext;
import org.kie.kogito.incubation.rules.services.StatefulRuleUnitService;
import org.kie.kogito.rules.RuleUnits;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.stream.Stream;

@ApplicationScoped
public class QuarkusStatefulRuleUnitService implements StatefulRuleUnitService {
    @Inject
    RuleUnits ruleUnits;
    StatefulRuleUnitServiceImpl delegate;

    @PostConstruct
    void startup() {
        delegate = new StatefulRuleUnitServiceImpl(ruleUnits);
    }

    @Override
    public MetaDataContext create(LocalId localId, ExtendedDataContext extendedDataContext) {
        return delegate.create(localId, extendedDataContext);
    }

    @Override
    public MetaDataContext dispose(LocalId localId) {
        return delegate.dispose(localId);
    }

    @Override
    public MetaDataContext fire(LocalId localId) {
        return delegate.fire(localId);
    }

    @Override
    public Stream<ExtendedDataContext> query(LocalId localId, ExtendedDataContext params) {
        return delegate.query(localId, params);
    }
}
