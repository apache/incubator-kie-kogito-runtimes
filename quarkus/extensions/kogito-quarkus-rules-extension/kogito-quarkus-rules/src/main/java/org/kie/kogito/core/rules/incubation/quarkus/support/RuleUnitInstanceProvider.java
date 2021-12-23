package org.kie.kogito.core.rules.incubation.quarkus.support;

import org.kie.kogito.incubation.common.ExtendedDataContext;
import org.kie.kogito.incubation.common.ExtendedReferenceContext;
import org.kie.kogito.incubation.common.MetaDataContext;
import org.kie.kogito.incubation.common.ReferenceContext;
import org.kie.kogito.incubation.rules.InstanceQueryId;
import org.kie.kogito.incubation.rules.RuleUnitId;
import org.kie.kogito.incubation.rules.RuleUnitIds;
import org.kie.kogito.incubation.rules.RuleUnitInstanceId;
import org.kie.kogito.incubation.rules.services.StatefulRuleUnitService;
import org.kie.kogito.incubation.rules.services.adapters.RuleUnitInstance;
import org.kie.kogito.incubation.rules.services.contexts.RuleUnitMetaDataContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.stream.Stream;

@ApplicationScoped
public class RuleUnitInstanceProvider {
    @Produces
    <T extends ReferenceContext> RuleUnitInstance<T> createRuleUnitInstance(
            RuleUnitIds componentRoot,
            StatefulRuleUnitService svc,
            ReferenceContext ctx) {

        Class<?> aClass = ctx.getClass();
        while (aClass.isSynthetic() && null != aClass.getSuperclass()) {
            aClass = aClass.getSuperclass();
        }

        RuleUnitId ruleUnitId = componentRoot.get(aClass);

        MetaDataContext result = svc.create(ruleUnitId, ExtendedReferenceContext.ofData(ctx));
        RuleUnitInstanceId instanceId = result.as(RuleUnitMetaDataContext.class).id(RuleUnitInstanceId.class);

        return new RuleUnitInstance<>() {

            @Override
            public RuleUnitInstanceId id() {
                return instanceId;
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
            public Stream<ExtendedDataContext> query(InstanceQueryId queryId, ExtendedReferenceContext ctx) {
                return svc.query(queryId, ctx);
            }
        };
    }
}
