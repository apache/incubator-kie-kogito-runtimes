/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.codegen.decision;

import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.GeneratorContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DecisionValidationTest {

    @Test
    public void testDefault() throws Exception {
        DecisionCodegen codeGenerator = DecisionCodegen.ofPath(Paths.get("src/test/resources/decision-validation-duplicateName").toAbsolutePath());
        codeGenerator.setContext(GeneratorContext.ofProperties(new Properties()));

        assertThrows(RuntimeException.class,
                     () -> {
                         codeGenerator.generate();
                     },
                     "Expected Validation would have failed for defaults.");
    }

    @Test
    public void testIgnore() throws Exception {
        DecisionCodegen codeGenerator = DecisionCodegen.ofPath(Paths.get("src/test/resources/decision-validation-duplicateName").toAbsolutePath());
        Properties props = new Properties();
        props.setProperty(DecisionCodegen.VALIDATION_CONFIGURATION_KEY, "IGNORE");
        codeGenerator.setContext(GeneratorContext.ofProperties(props));

        List<GeneratedFile> files = codeGenerator.generate();
        Assertions.assertThat(files).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    public void testDisabled() throws Exception {
        DecisionCodegen codeGenerator = DecisionCodegen.ofPath(Paths.get("src/test/resources/decision-validation-duplicateName").toAbsolutePath());
        Properties props = new Properties();
        props.setProperty(DecisionCodegen.VALIDATION_CONFIGURATION_KEY, "DISABLED");
        codeGenerator.setContext(GeneratorContext.ofProperties(props));

        List<GeneratedFile> files = codeGenerator.generate();
        Assertions.assertThat(files).hasSizeGreaterThanOrEqualTo(1);
    }
}