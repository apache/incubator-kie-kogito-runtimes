package org.kie.kogito.core.rules.incubation.quarkus.support.adapters;

import org.kie.kogito.incubation.common.ExtendedDataContext;
import org.kie.kogito.incubation.common.ExtendedReferenceContext;
import org.kie.kogito.incubation.common.MetaDataContext;
import org.kie.kogito.incubation.common.ReferenceContext;
import org.kie.kogito.incubation.rules.RuleUnitInstanceId;
import org.kie.kogito.incubation.rules.services.StatefulRuleUnitService;
import org.kie.kogito.incubation.rules.services.adapters.RuleUnitInstance;

import java.util.stream.Stream;

class QuarkusRuleUnitInstanceImpl<T extends ReferenceContext> implements RuleUnitInstance<T> {

    private final RuleUnitInstanceId instanceId;
    private final T ctx;
    private final StatefulRuleUnitService svc;

    public QuarkusRuleUnitInstanceImpl(RuleUnitInstanceId instanceId, T ctx, StatefulRuleUnitService svc) {
        this.instanceId = instanceId;
        this.ctx = ctx;
        this.svc = svc;
    }

    @Override
    public RuleUnitInstanceId id() {
        return instanceId;
    }

    @Override
    public T context() {
        return ctx;
    }

    @Override
    public MetaDataContext fire() {
        return svc.fire(instanceId);
    }

    @Override
    public MetaDataContext dispose() {
        return svc.dispose(instanceId);
    }

    @Override
    public Stream<ExtendedDataContext> query(String queryId, ExtendedReferenceContext ctx) {
        return svc.query(instanceId.queries().get(queryId), ctx);
    }
}