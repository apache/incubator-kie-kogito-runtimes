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
package org.kie.kogito.codegen.core;

import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.api.AddonsConfig;
import org.kie.kogito.codegen.api.ConfigGenerator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationConfigGeneratorTest {

    private static final KogitoBuildContext context = JavaKogitoBuildContext.builder()
            .withPackageName("org.kie.kogito.test")
            .withAddonsConfig(AddonsConfig.DEFAULT)
            .build();

    @Test
    public void withConfig() {
        final ApplicationConfigGenerator generator = new ApplicationConfigGenerator(context);
        final ConfigGenerator mockGenerator = Mockito.mock(ConfigGenerator.class);
        final ApplicationConfigGenerator returnedConfigGenerator = generator.withConfigGenerator(mockGenerator);
        assertThat(returnedConfigGenerator)
                .isNotNull()
                .isSameAs(generator);
    }

    @Test
    public void withConfigNull() {
        final ApplicationConfigGenerator generator = new ApplicationConfigGenerator(context);
        final ApplicationConfigGenerator returnedConfigGenerator = generator.withConfigGenerator(null);
        assertThat(returnedConfigGenerator)
                .isNotNull()
                .isSameAs(generator);
    }

}
