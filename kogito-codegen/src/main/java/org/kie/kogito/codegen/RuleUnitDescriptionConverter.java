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
package org.kie.kogito.codegen;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kie.kogito.rules.RuleUnitConfig;

public class RuleUnitDescriptionConverter {

    public static RuleUnitDescription from(org.kie.internal.ruleunit.RuleUnitDescription droolDesc) {
        return droolDesc instanceof RuleUnitDescription ? ((RuleUnitDescription)droolDesc) : new BridgeRuleUnitDescription(droolDesc);
    }

    private static class BridgeRuleUnitDescription implements RuleUnitDescription {

        private org.kie.internal.ruleunit.RuleUnitDescription droolDesc;

        public BridgeRuleUnitDescription(org.kie.internal.ruleunit.RuleUnitDescription droolDesc) {
            this.droolDesc = droolDesc;
        }

        @Override
        public Class<?> getRuleUnitClass() {
            return droolDesc.getRuleUnitClass();
        }

        @Override
        public String getRuleUnitName() {
            return droolDesc.getRuleUnitName();
        }

        @Override
        public String getCanonicalName() {
            return droolDesc.getCanonicalName();
        }

        @Override
        public String getSimpleName() {
            return droolDesc.getSimpleName();
        }

        @Override
        public String getPackageName() {
            return droolDesc.getPackageName();
        }

        @Override
        public Optional<Class<?>> getDatasourceType(String name) {
            return droolDesc.getDatasourceType(name);
        }

        @Override
        public Optional<Class<?>> getVarType(String name) {
            return droolDesc.getVarType(name);
        }

        @Override
        public boolean hasVar(String name) {
            return droolDesc.hasVar(name);
        }

        @Override
        public RuleUnitVariable getVar(String name) {
            return droolDesc.getUnitVarDeclarations().stream().filter(r -> r.getName().equals(name)).findAny().map(
                    RuleUnitVariableConverter::from).orElse(null);
        }

        @Override
        public Collection<String> getUnitVars() {
            return droolDesc.getUnitVars();
        }

        @Override
        public Collection<? extends RuleUnitVariable> getUnitVarDeclarations() {
            return droolDesc.getUnitVarDeclarations().stream().map(RuleUnitVariableConverter::from).collect(Collectors
                    .toList());
        }

        @Override
        public boolean hasDataSource(String name) {
            return droolDesc.hasDataSource(name);
        }

        @Override
        public RuleUnitConfig getConfig() {
            return null;
        }

    }

}
