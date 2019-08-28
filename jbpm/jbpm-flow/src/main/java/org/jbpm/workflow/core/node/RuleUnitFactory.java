package org.jbpm.workflow.core.node;

import org.kie.api.runtime.process.ProcessContext;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitMemory;

public interface RuleUnitFactory<T extends RuleUnitMemory> {
    T bind(ProcessContext ctx);
    RuleUnit<T> unit();
    void unbind(ProcessContext ctx, T model);
}
