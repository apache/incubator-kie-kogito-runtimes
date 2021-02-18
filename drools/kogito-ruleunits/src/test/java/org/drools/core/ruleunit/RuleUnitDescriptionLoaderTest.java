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
package org.drools.core.ruleunit;

import java.util.Optional;

import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.ruleunit.RuleUnitDescriptionRegistry.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Disabled
public class RuleUnitDescriptionLoaderTest {

    private RuleUnitDescriptionLoader loader;

    @BeforeEach
    public void prepareRuleUnitDescriptionLoader() {
        loader = RuleUnitTestUtil.createRuleUnitDescriptionLoader();
    }

    @Test
    public void getStateUnitExists() {
        assertThat(loader.getState()).isEqualTo(State.UNKNOWN);
        assertDescriptionIsLoaded();
        assertThat(loader.getState()).isEqualTo(State.UNIT);
    }

    @Test
    public void getStateUnitNotExists() {
        assertThat(loader.getState()).isEqualTo(State.UNKNOWN);
        final Optional<RuleUnitDescription> description = loader.getDescription("nonexisting");
        assertThat(description).isNotPresent();
        assertThat(loader.getState()).isEqualTo(State.NO_UNIT);
    }

    @Test
    public void getStateMixWithAndWithoutUnit() {
        assertThat(loader.getState()).isEqualTo(State.UNKNOWN);
        assertDescriptionIsLoaded();
        assertThat(loader.getState()).isEqualTo(State.UNIT);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> loader.getDescription("nonexisting"));
    }

    @Test
    public void getDescriptions() {
        assertThat(loader.getDescriptions()).isEmpty();
        assertDescriptionIsLoaded();
        assertThat(loader.getDescriptions()).hasSize(1);
    }

    @Test
    public void getDescriptionFromRuleImpl() {
        final RuleImpl ruleImpl = Mockito.mock(RuleImpl.class);
        Mockito.when(ruleImpl.getRuleUnitClassName()).thenReturn(TestRuleUnit.class.getName());

        final Optional<RuleUnitDescription> description = loader.getDescription(ruleImpl);
        assertThat(description).isPresent();
        final Optional<RuleUnitDescription> description2 = loader.getDescription(ruleImpl);
        assertThat(description).isPresent();
        assertThat(description.get()).isSameAs(description2.get());
        assertThat(description.get().getRuleUnitClass()).isEqualTo(TestRuleUnit.class);
    }

    @Test
    public void getDescriptionFromUnitClassName() {
        final RuleUnitDescription description = assertDescriptionIsLoaded();
        final RuleUnitDescription description2 = assertDescriptionIsLoaded();
        assertThat(description).isSameAs(description2);
        assertThat(description.getRuleUnitClass()).isEqualTo(TestRuleUnit.class);
    }

    private RuleUnitDescription assertDescriptionIsLoaded() {
        final Optional<RuleUnitDescription> description = loader.getDescription(TestRuleUnit.class.getName());
        assertThat(description).isPresent();
        return description.get();
    }
}
