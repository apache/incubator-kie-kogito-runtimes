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

package org.kie.kogito.rules.units;

public class SimpleRuleUnitDescription extends AbstractRuleUnitDescription {
    private final String name;
    private final String packageName;
    private final String simpleName;

    public SimpleRuleUnitDescription(String name) {
        this.name = name;
        this.simpleName = name.substring(name.lastIndexOf('.') + 1);
        this.packageName = name.substring(0, name.lastIndexOf('.'));
    }

    @Override
    @Deprecated
    public Class<?> getRuleUnitClass() {
        return null;
    }

    @Override
    public String getCanonicalName() {
        return getRuleUnitName();
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getRuleUnitName() {
        return name;
    }
}
