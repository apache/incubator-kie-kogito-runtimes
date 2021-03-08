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

package org.kie.kogito.test.engine.subprocess;

import org.junit.jupiter.api.Test;
import org.kie.kogito.junit.api.KogitoUnitTestContext;
import org.kie.kogito.junit.api.KogitoUnitTestDeployment;
import org.kie.kogito.junit.api.KogitoUnitTestDeploymentException;
import org.kie.kogito.junit.api.KogitoUnitTestResource;
import org.kie.kogito.junit.listeners.FlowProcessEventListenerTracker;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AdHocSubprocessTest {

    private static final String EMPTY_COMPLETION_CONDITION =
            "org/kie/kogito/test/engine/subprocess/AdHocSubprocess-emptyCompletionCondition.bpmn2";

    @Test
    @KogitoUnitTestDeployment(
            resources = { @KogitoUnitTestResource(path = EMPTY_COMPLETION_CONDITION) },
            listeners = { FlowProcessEventListenerTracker.class })
    @KogitoUnitTestDeploymentException
    public void testEmptyCompletionCondition(KogitoUnitTestContext context) {
        assertNotNull(context.find(Throwable.class));
    }

}
