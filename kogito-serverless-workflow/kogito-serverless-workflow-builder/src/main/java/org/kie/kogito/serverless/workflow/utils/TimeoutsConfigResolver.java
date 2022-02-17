/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.serverless.workflow.utils;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.timeouts.TimeoutsDefinition;

public class TimeoutsConfigResolver {

    private static final String NON_NEGATIVE_DURATION_MUST_BE_PROVIDED = "A positive ISO 8601 duration must be provided when it is configured.";

    private static final String INVALID_EVENT_TIMEOUT_FOR_STATE_ERROR = "An invalid \"eventTimeout\": \"%s\" configuration was provided for the state \"%s\" in the serverless workflow: \"%s\". " +
            NON_NEGATIVE_DURATION_MUST_BE_PROVIDED;

    private static final String INVALID_EVENT_TIMEOUT_FOR_WORKFLOW_ERROR = "An invalid \"eventTimeout\": \"%s\" configuration was provided for the serverless workflow: \"%s\". " +
            NON_NEGATIVE_DURATION_MUST_BE_PROVIDED;

    private TimeoutsConfigResolver() {
    }

    public static String resolveEventTimeout(State state, Workflow workflow) {
        TimeoutsDefinition timeouts = state.getTimeouts();
        if (timeouts != null && timeouts.getEventTimeout() != null) {
            validateStateTimeoutValue(state, workflow, timeouts.getEventTimeout());
            return timeouts.getEventTimeout();
        } else {
            timeouts = workflow.getTimeouts();
            if (timeouts != null && timeouts.getEventTimeout() != null) {
                validateWorkflowTimeoutValue(workflow, timeouts.getEventTimeout());
                return timeouts.getEventTimeout();
            }
        }
        return null;
    }

    private static void validateStateTimeoutValue(State state, Workflow workflow, String timeoutValue) {
        if (isInvalidDuration(timeoutValue)) {
            throw new IllegalArgumentException(String.format(INVALID_EVENT_TIMEOUT_FOR_STATE_ERROR, timeoutValue, state.getName(), workflow.getName()));
        }
    }

    private static void validateWorkflowTimeoutValue(Workflow workflow, String timeoutValue) {
        if (isInvalidDuration(timeoutValue)) {
            throw new IllegalArgumentException(String.format(INVALID_EVENT_TIMEOUT_FOR_WORKFLOW_ERROR, timeoutValue, workflow.getName()));
        }
    }

    private static boolean isInvalidDuration(String value) {
        Duration duration = parseOrIgnoreException(value);
        return duration == null || duration.isNegative() || duration.isZero();
    }

    private static Duration parseOrIgnoreException(String duration) {
        try {
            return Duration.parse(duration);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}