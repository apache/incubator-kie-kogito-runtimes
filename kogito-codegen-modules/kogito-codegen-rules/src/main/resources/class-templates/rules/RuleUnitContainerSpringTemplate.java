/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package $Package$;

import org.drools.core.common.ReteEvaluator;
import org.drools.ruleunits.api.RuleUnit;
import org.drools.ruleunits.api.RuleUnitData;
import org.drools.ruleunits.impl.InternalRuleUnit;
import org.drools.ruleunits.impl.sessions.RuleUnitExecutorImpl;
import org.kie.kogito.rules.RuleEventListenerConfig;

@org.springframework.web.context.annotation.ApplicationScope
@org.springframework.stereotype.Component
public class RuleUnits extends org.kie.kogito.drools.core.unit.AbstractRuleUnits implements org.kie.kogito.rules.RuleUnits {

    @org.springframework.beans.factory.annotation.Autowired
    Application application;

    @Override
    protected <T extends RuleUnitData> RuleUnit<T> internalCreate(Class<T> clazz) {
        String fqcn = clazz.getCanonicalName();
        switch(fqcn) {
            case "$RuleUnit$":
                return (RuleUnit<T>) new $RuleUnit$RuleUnit(this);
            default:
                throw new java.lang.UnsupportedOperationException();
        }
    }

    @Override
    public void register(RuleUnit<?> unit) {
        registerRuleUnit(application, (InternalRuleUnit<?>) unit);
    }
}
