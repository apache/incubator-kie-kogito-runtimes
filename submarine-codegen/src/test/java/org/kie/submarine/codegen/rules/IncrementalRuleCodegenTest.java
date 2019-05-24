/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.submarine.codegen.rules;

import org.junit.Test;
import org.kie.api.io.ResourceType;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

//TODO
public class IncrementalRuleCodegenTest {

    @Test
    public void testConstructorSupportedResourceTypes() {
        new IncrementalRuleCodegen(null, null, ResourceType.DRL);
        new IncrementalRuleCodegen(null, null, ResourceType.DTABLE);
    }

    @Test
    public void testConstructorUnsupportedResourceType() {
        assertThatThrownBy(() -> new IncrementalRuleCodegen(null, null, ResourceType.BPMN2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void setPackageName() {

    }

    @Test
    public void factoryMethods() {
    }

    @Test
    public void generate() {
    }

    @Test
    public void updateConfig() {
    }

    @Test
    public void setDependencyInjection() {
    }

    @Test
    public void applicationBodyDeclaration() {
    }
}