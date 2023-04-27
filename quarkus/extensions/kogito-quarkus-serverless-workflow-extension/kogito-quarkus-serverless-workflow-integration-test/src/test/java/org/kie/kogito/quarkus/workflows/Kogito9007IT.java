/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.quarkus.workflows;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;

import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.assertProcessInstanceHasFinished;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.newProcessInstanceAndGetId;

@QuarkusIntegrationTest
class Kogito9007IT {

    private static final String KOGITO_9007_EVENT_STATE_TIMEOUTS_URL = "/kogito_9007_event_state_timeouts";
    private static final String KOGITO_9007_EVENT_STATE_TIMEOUTS_GET_BY_ID_URL = KOGITO_9007_EVENT_STATE_TIMEOUTS_URL + "/{id}";
    private static final String EMPTY_WORKFLOW_DATA = "{\"workflowdata\" : \"\"}";
    private static final int AT_LEAST_SECONDS = 1;
    private static final int AT_MOST_SECONDS = 120;

    @Test
    void eventStateTimeouts() {
        // Start 3 simultaneous instances.
        String processInstanceId1 = newProcessInstanceAndGetId(KOGITO_9007_EVENT_STATE_TIMEOUTS_URL, EMPTY_WORKFLOW_DATA);
        String processInstanceId2 = newProcessInstanceAndGetId(KOGITO_9007_EVENT_STATE_TIMEOUTS_URL, EMPTY_WORKFLOW_DATA);
        String processInstanceId3 = newProcessInstanceAndGetId(KOGITO_9007_EVENT_STATE_TIMEOUTS_URL, EMPTY_WORKFLOW_DATA);

        // The three instances must finish in a period of time, otherwise the issue is still present.
        assertProcessInstanceHasFinished(KOGITO_9007_EVENT_STATE_TIMEOUTS_GET_BY_ID_URL, processInstanceId1, AT_LEAST_SECONDS, AT_MOST_SECONDS);
        assertProcessInstanceHasFinished(KOGITO_9007_EVENT_STATE_TIMEOUTS_GET_BY_ID_URL, processInstanceId2, AT_LEAST_SECONDS, AT_MOST_SECONDS);
        assertProcessInstanceHasFinished(KOGITO_9007_EVENT_STATE_TIMEOUTS_GET_BY_ID_URL, processInstanceId3, AT_LEAST_SECONDS, AT_MOST_SECONDS);
    }
}
