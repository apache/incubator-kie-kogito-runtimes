package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.common.ExtendedDataContext;
import org.kie.kogito.incubation.common.ExtendedReferenceContext;
import org.kie.kogito.incubation.common.MetaDataContext;
import org.kie.kogito.incubation.rules.InstanceQueryId;
import org.kie.kogito.incubation.rules.RuleUnitInstanceId;

import java.util.stream.Stream;

public interface RuleUnitInstance<T> {
    RuleUnitInstanceId id();
    MetaDataContext fire();
    MetaDataContext dispose();

    Stream<ExtendedDataContext> query(InstanceQueryId queryId, ExtendedReferenceContext ctx);
}
