/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.tracing.decision.event.evaluate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.event.AfterEvaluateAllEvent;
import org.kie.dmn.api.core.event.AfterEvaluateBKMEvent;
import org.kie.dmn.api.core.event.AfterEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.AfterInvokeBKMEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateAllEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateBKMEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.BeforeInvokeBKMEvent;
import org.kie.dmn.api.core.event.DMNEvent;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.dmn.feel.util.Pair;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The purpose of this test is ensure that the structure of {@link DMNRuntimeEventListener} remains
 * aligned with our {@link EvaluateEventType} enum that maps {@link DMNEvent} to {@link EvaluateEvent}.
 */
class EvaluateEventTypeTest {

    private static final Map<EvaluateEventType, Pair<String, Class<?>>> CHECK_MAP = new HashMap<EvaluateEventType, Pair<String, Class<?>>>() {{
        put(EvaluateEventType.BEFORE_EVALUATE_ALL, new Pair<>("beforeEvaluateAll", BeforeEvaluateAllEvent.class));
        put(EvaluateEventType.AFTER_EVALUATE_ALL, new Pair<>("afterEvaluateAll", AfterEvaluateAllEvent.class));
        put(EvaluateEventType.BEFORE_EVALUATE_BKM, new Pair<>("beforeEvaluateBKM", BeforeEvaluateBKMEvent.class));
        put(EvaluateEventType.AFTER_EVALUATE_BKM, new Pair<>("afterEvaluateBKM", AfterEvaluateBKMEvent.class));
        put(EvaluateEventType.BEFORE_EVALUATE_CONTEXT_ENTRY, new Pair<>("beforeEvaluateContextEntry", BeforeEvaluateContextEntryEvent.class));
        put(EvaluateEventType.AFTER_EVALUATE_CONTEXT_ENTRY, new Pair<>("afterEvaluateContextEntry", AfterEvaluateContextEntryEvent.class));
        put(EvaluateEventType.BEFORE_EVALUATE_DECISION, new Pair<>("beforeEvaluateDecision", BeforeEvaluateDecisionEvent.class));
        put(EvaluateEventType.AFTER_EVALUATE_DECISION, new Pair<>("afterEvaluateDecision", AfterEvaluateDecisionEvent.class));
        put(EvaluateEventType.BEFORE_EVALUATE_DECISION_SERVICE, new Pair<>("beforeEvaluateDecisionService", BeforeEvaluateDecisionServiceEvent.class));
        put(EvaluateEventType.AFTER_EVALUATE_DECISION_SERVICE, new Pair<>("afterEvaluateDecisionService", AfterEvaluateDecisionServiceEvent.class));
        put(EvaluateEventType.BEFORE_EVALUATE_DECISION_TABLE, new Pair<>("beforeEvaluateDecisionTable", BeforeEvaluateDecisionTableEvent.class));
        put(EvaluateEventType.AFTER_EVALUATE_DECISION_TABLE, new Pair<>("afterEvaluateDecisionTable", AfterEvaluateDecisionTableEvent.class));
        put(EvaluateEventType.BEFORE_INVOKE_BKM, new Pair<>("beforeInvokeBKM", BeforeInvokeBKMEvent.class));
        put(EvaluateEventType.AFTER_INVOKE_BKM, new Pair<>("afterInvokeBKM", AfterInvokeBKMEvent.class));
    }};
    private static final Class<DMNRuntimeEventListener> LISTENER_CLASS = DMNRuntimeEventListener.class;

    @Test
    void testExistingEvents() {
        CHECK_MAP.forEach((type, checkPair) ->
                assertDoesNotThrow(
                        () -> LISTENER_CLASS.getDeclaredMethod(checkPair.getLeft(), checkPair.getRight()),
                        () -> String.format("Listener method \"%s(%s)\" not found for EvaluateEventType.%s", checkPair.getLeft(), checkPair.getRight().getSimpleName(), type)
                )
        );
    }

    @Test
    void testNotManagedEvents() {
        for (Method listenerMethod : LISTENER_CLASS.getMethods()) {
            Optional<Map.Entry<EvaluateEventType, Pair<String, Class<?>>>> optEntry = CHECK_MAP.entrySet().stream()
                    .filter(e -> e.getValue().getLeft().equals(listenerMethod.getName()))
                    .findAny();
            assertTrue(
                    optEntry.isPresent(),
                    () -> String.format("No EvaluateEventType for listener method \"%s\"", listenerMethod.getName())
            );
        }
    }

    @Test
    void testNotManagedTypes() {
        for (EvaluateEventType t : EvaluateEventType.values()) {
            assertTrue(CHECK_MAP.containsKey(t), () -> String.format("No test entry for EvaluateEventType.%s", t));
        }
    }
}
