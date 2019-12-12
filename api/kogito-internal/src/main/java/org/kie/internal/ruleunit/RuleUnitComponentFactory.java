/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.internal.ruleunit;

import org.kie.api.definition.KiePackage;
import org.kie.api.internal.utils.ServiceRegistry;

public interface RuleUnitComponentFactory {
    class FactoryHolder {
        private static final RuleUnitComponentFactory factory = ServiceRegistry.getInstance().get(RuleUnitComponentFactory.class);
    }

    static RuleUnitComponentFactory get() {
        return FactoryHolder.factory;
    }

    RuleUnitDescription createRuleUnitDescription( KiePackage pkg, Class<?> ruleUnitClass );

    ApplyPmmlModelCommandExecutor newApplyPmmlModelCommandExecutor();

    boolean isRuleUnitClass( Class<?> ruleUnitClass );
    boolean isDataSourceClass( Class<?> ruleUnitClass );

    boolean isLegacyRuleUnit();
}
