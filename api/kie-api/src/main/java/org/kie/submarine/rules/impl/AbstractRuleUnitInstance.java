/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.submarine.rules.impl;

import java.lang.reflect.Field;

import org.kie.api.runtime.KieSession;
import org.kie.submarine.rules.RuleUnit;
import org.kie.submarine.rules.RuleUnitInstance;

public class AbstractRuleUnitInstance<T> implements RuleUnitInstance<T> {

    private final T workingMemory;
    private final RuleUnit<T> unit;
    private final KieSession rt;

    public AbstractRuleUnitInstance(RuleUnit<T> unit, T workingMemory, KieSession rt) {
        this.unit = unit;
        this.rt = rt;
        this.workingMemory = workingMemory;
    }

    public void fire() {
        magicReflectionThingie(rt, workingMemory);
        rt.fireAllRules();
    }

    @Override
    public RuleUnit<T> unit() {
        return unit;
    }

    public T workingMemory() {
        return workingMemory;
    }

    private void magicReflectionThingie(KieSession rt, T workingMemory) {
        try {
            for (Field f : workingMemory.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object v = null;
                v = f.get(workingMemory);
                if (v instanceof ListDataSource) {
                    ListDataSource o = (ListDataSource) v;
                    o.drainInto(rt::insert);
                }
            }
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }
}
