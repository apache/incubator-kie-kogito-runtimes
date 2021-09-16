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
package com.myspace.demo;

import java.util.List;

import org.kie.kogito.eventdriven.rules.AbstractEventDrivenQueryExecutor;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitInstance;

public class $unit$Query$name$EventDrivenExecutor extends AbstractEventDrivenQueryExecutor {

    RuleUnit<$UnitType$> ruleUnit;

    public String getQueryId() {
        return "$endpointName$";
    };

    public Object executeQuery(CloudEvent input) {
        // $UnitTypeDTO$ unitDTO
        RuleUnitInstance<$UnitType$> instance = ruleUnit.createInstance();
        // Do not return the result directly to allow post execution codegen (like monitoring)
        List<$ReturnType$> response = instance.executeQuery($unit$Query$name$.class);
        instance.dispose();
        return response;
    }
}
