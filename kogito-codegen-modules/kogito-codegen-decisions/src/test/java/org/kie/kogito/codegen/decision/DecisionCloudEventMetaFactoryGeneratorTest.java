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

package org.kie.kogito.codegen.decision;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.api.AddonsConfig;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.core.io.CollectedResourceProducer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionCloudEventMetaFactoryGeneratorTest {

    private static final String MODEL_PATH = "src/test/resources/decision/models/vacationDays";
    private static final int EXPECTED_FILES_WITH_CLOUDEVENTS = 5;
    private static final int EXPECTED_FILES_WITHOUT_CLOUDEVENTS = 4;
    private static final String EXPECTED_FILE_PATH = "org/kie/kogito/app/DecisionCloudEventMetaFactory.java";

    @Test
    void testGenerateWithCloudEventsEnabled() {
        DecisionCodegen codeGenerator = buildCodegen(true);
        List<GeneratedFile> generatedFiles = codeGenerator.generate();

        assertEquals(EXPECTED_FILES_WITH_CLOUDEVENTS, generatedFiles.size());
        assertTrue(generatedFiles.stream().anyMatch(gf -> EXPECTED_FILE_PATH.equals(gf.relativePath())));
    }

    @Test
    void testGenerateWithCloudEventsDisabled() {
        DecisionCodegen codeGenerator = buildCodegen(false);
        List<GeneratedFile> generatedFiles = codeGenerator.generate();

        assertEquals(EXPECTED_FILES_WITHOUT_CLOUDEVENTS, generatedFiles.size());
        assertFalse(generatedFiles.stream().anyMatch(gf -> EXPECTED_FILE_PATH.equals(gf.relativePath())));
    }

    private static DecisionCodegen buildCodegen(boolean withCloudEvents) {
        KogitoBuildContext context = QuarkusKogitoBuildContext.builder()
                .withAddonsConfig(AddonsConfig.builder().withCloudEvents(withCloudEvents).build())
                .build();

        Collection<CollectedResource> collectedResources = CollectedResourceProducer
                .fromPaths(Paths.get(MODEL_PATH).toAbsolutePath());

        return DecisionCodegen.ofCollectedResources(context, collectedResources);
    }

}
