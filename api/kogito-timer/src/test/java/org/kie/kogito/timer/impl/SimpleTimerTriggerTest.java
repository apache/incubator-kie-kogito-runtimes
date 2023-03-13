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

package org.kie.kogito.timer.impl;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTimerTriggerTest {

    public static final OffsetDateTime TRIGGER_START_TIME = OffsetDateTime.parse("2023-03-06T14:00:00.000+05:00");

    public static final long PERIOD = 10;

    @ParameterizedTest
    @MethodSource("intervalTriggerParams")
    void simpleTimerTrigger(OffsetDateTime startTimeAsOffsetDateTime, long period, ChronoUnit periodUnit,
            long repeatLimit, OffsetDateTime endTimeAsOffsetDateTime, String offsetId, OffsetDateTime[] expectedExecutions) {
        Date startTime = Date.from(startTimeAsOffsetDateTime.toInstant());
        Date endTime = endTimeAsOffsetDateTime != null ? Date.from(endTimeAsOffsetDateTime.toInstant()) : null;
        SimpleTimerTrigger trigger = new SimpleTimerTrigger(startTime, period, periodUnit, repeatLimit, endTime, offsetId);
        List<Date> nextFireTimes = new ArrayList<>();
        while (trigger.hasNextFireTime() != null) {
            nextFireTimes.add(trigger.nextFireTime());
        }
        assertThat(nextFireTimes).hasSize(expectedExecutions.length);
        for (int i = 0; i < expectedExecutions.length; i++) {
            assertThat(nextFireTimes.get(i)).isEqualTo(expectedExecutions[i].toInstant());
            OffsetDateTime nextFireTimeAsOffsetDateTime = OffsetDateTime.ofInstant(nextFireTimes.get(i).toInstant(), ZoneOffset.of(trigger.getTimeOffsetId()));
            assertThat(nextFireTimeAsOffsetDateTime).isEqualTo(expectedExecutions[i]);
        }
    }

    private static Stream<Arguments> intervalTriggerParams() {
        return Stream.of(
                Arguments.of(TRIGGER_START_TIME, PERIOD, ChronoUnit.SECONDS, 0, null, TRIGGER_START_TIME.getOffset().getId(),
                        new OffsetDateTime[] {
                                TRIGGER_START_TIME }),

                Arguments.of(TRIGGER_START_TIME, PERIOD, ChronoUnit.SECONDS, 1, null, TRIGGER_START_TIME.getOffset().getId(),
                        new OffsetDateTime[] {
                                TRIGGER_START_TIME,
                                TRIGGER_START_TIME.plus(PERIOD, ChronoUnit.SECONDS)
                        }),
                Arguments.of(TRIGGER_START_TIME, PERIOD, ChronoUnit.SECONDS, 2, null, TRIGGER_START_TIME.getOffset().getId(),
                        new OffsetDateTime[] {
                                TRIGGER_START_TIME,
                                TRIGGER_START_TIME.plus(PERIOD, ChronoUnit.SECONDS),
                                TRIGGER_START_TIME.plus(PERIOD * 2, ChronoUnit.SECONDS)
                        }),
                Arguments.of(TRIGGER_START_TIME, PERIOD, ChronoUnit.SECONDS, 3, null, TRIGGER_START_TIME.getOffset().getId(),
                        new OffsetDateTime[] {
                                TRIGGER_START_TIME,
                                TRIGGER_START_TIME.plus(PERIOD, ChronoUnit.SECONDS),
                                TRIGGER_START_TIME.plus(PERIOD * 2, ChronoUnit.SECONDS),
                                TRIGGER_START_TIME.plus(PERIOD * 3, ChronoUnit.SECONDS)
                        }));
    }
}
