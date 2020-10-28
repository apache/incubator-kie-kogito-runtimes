/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.internal.ruleunit;

public class RuleUnitVariableConverter {

    public static RuleUnitVariable from(org.kie.internal.ruleunit.RuleUnitVariable unit) {
        return unit instanceof RuleUnitVariable ? (RuleUnitVariable) unit: new BridgeRuleUnitVariable(unit);
    }

    private static class BridgeRuleUnitVariable implements RuleUnitVariable {

        private org.kie.internal.ruleunit.RuleUnitVariable unit;

        public BridgeRuleUnitVariable(org.kie.internal.ruleunit.RuleUnitVariable unit) {
            this.unit = unit;
        }

        @Override
        public boolean isDataSource() {
            return unit.isDataSource();
        }

        @Override
        public String getName() {
            return unit.getName();
        }

        @Override
        public String getter() {
           return unit.getter();
        }

        @Override
        public String setter() {
           throw new UnsupportedOperationException();
        }

        @Override
        public Class<?> getType() {
            return unit.getType();
        }

        @Override
        public Class<?> getDataSourceParameterType() {
            return unit.getDataSourceParameterType();
        }

        @Override
        public Class<?> getBoxedVarType() {
            return unit.getBoxedVarType();
        }

    }
}
