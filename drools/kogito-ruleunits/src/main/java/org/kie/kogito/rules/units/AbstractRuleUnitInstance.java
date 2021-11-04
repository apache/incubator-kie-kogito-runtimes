/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.rules.units;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.drools.core.common.ReteEvaluator;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.time.SessionClock;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitData;
import org.kie.kogito.rules.RuleUnitInstance;
import org.kie.kogito.rules.RuleUnitQuery;

public class AbstractRuleUnitInstance<T extends RuleUnitData> implements RuleUnitInstance<T> {

    private final T unitMemory;
    private final RuleUnit<T> unit;
    private final ReteEvaluator reteEvaluator;

    public AbstractRuleUnitInstance(RuleUnit<T> unit, T unitMemory, ReteEvaluator reteEvaluator) {
        this.unit = unit;
        this.reteEvaluator = reteEvaluator;
        this.unitMemory = unitMemory;
        bind(reteEvaluator, unitMemory);
    }

    @Override
    public int fire() {
        return reteEvaluator.fireAllRules();
    }

    @Override
    public void dispose() {
        reteEvaluator.dispose();
    }

    @Override
    public List<Map<String, Object>> executeQuery(String query) {
        fire();
        return reteEvaluator.getQueryResults(query).toList();
    }

    @Override
    public <Q> Q executeQuery(Class<? extends RuleUnitQuery<Q>> query) {
        return createRuleUnitQuery(query).execute();
    }

    protected <Q> RuleUnitQuery<Q> createRuleUnitQuery(Class<? extends RuleUnitQuery<Q>> query) {
        try {
            return query.getConstructor(RuleUnitInstance.class).newInstance(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RuleUnit<T> unit() {
        return unit;
    }

    @Override
    public <C extends SessionClock> C getClock() {
        return (C) reteEvaluator.getSessionClock();
    }

    public T workingMemory() {
        return unitMemory;
    }

    protected void bind(ReteEvaluator reteEvaluator, T workingMemory) {
        try {
            for (Field f : workingMemory.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object v = f.get(workingMemory);
                String dataSourceName = String.format(
                        "%s.%s", workingMemory.getClass().getCanonicalName(), f.getName());
                if (v instanceof DataSource) {
                    DataSource<?> o = (DataSource<?>) v;
                    EntryPoint ep = reteEvaluator.getEntryPoint(dataSourceName);
                    o.subscribe(new EntryPointDataProcessor(ep));
                }
                try {
                    reteEvaluator.setGlobal(dataSourceName, v);
                } catch (RuntimeException e) {
                    // ignore if the global doesn't exist
                }
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
}
