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

package org.kie.kogito.codegen.tests.basic;

import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.kie.kogito.app.Application;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicTest {

    Application app = new Application();

    @Test
    public void fireRuleTest() {
        KieSession kieSession =
                app.ruleUnits()
                        .ruleRuntimeBuilder()
                        .newKieSession();

        kieSession.insert(new Person());
        kieSession.fireAllRules();

        assertEquals(1, kieSession.getObjects(o -> o instanceof Result).size());
    }

}
