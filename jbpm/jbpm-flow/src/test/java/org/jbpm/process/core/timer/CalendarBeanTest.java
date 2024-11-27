/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.jbpm.process.core.timer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.END_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAYS;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAY_DATE_FORMAT;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.START_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.TIMEZONE;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.WEEKEND_DAYS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarBeanTest {

    @Test
    void requiredValidationMissingEndHour() {
        Properties config = new Properties();
        config.setProperty(START_HOUR, "25");
        StringBuilder errors = new StringBuilder();
        assertThat(errors).isEmpty();
        CalendarBean.requiredValidation(errors, config);
        assertThat(errors).isNotEmpty();
        String expected = "Property "+END_HOUR +" is required";
        assertThat(errors).contains(expected);

    }

    @Test
    void formatValidationWrongStartHour() {
        Properties config = new Properties();
        config.setProperty(START_HOUR, "25");
        StringBuilder errors = new StringBuilder();
        assertThat(errors).isEmpty();
        CalendarBean.formatValidation(errors, config);
        assertThat(errors).isNotEmpty();
        String expected = START_HOUR+" 25 outside expected boundaries (0-24)";
        assertThat(errors).contains(expected);

    }

    @ParameterizedTest
    @MethodSource("getInValidCalendarProperties")
    public void testValidationForInvalidProperties(Map<String, Object> propertyMap, List<String> errorMessages) {
        Properties businessCalendarProperties = new Properties();
        businessCalendarProperties.putAll(propertyMap);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->new CalendarBean(businessCalendarProperties));
        errorMessages.forEach(msg -> assertTrue(exception.getMessage().contains(msg)));
    }

    @Test
    public void testValidationForInvalidFormats() {
        Map<String, Object> propertyMap = Map.of(START_HOUR, "2", END_HOUR, "12", HOLIDAY_DATE_FORMAT, "aaa/y/d", HOLIDAYS,
                "22-12-121", TIMEZONE, "invalid/invalid");
        Properties businessCalendarProperties = new Properties();
        businessCalendarProperties.putAll(propertyMap);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CalendarBean(businessCalendarProperties));
        assertTrue(exception.getMessage().contains(HOLIDAYS+ " is not valid: Unparseable date: \"22-12-121\""));
        assertTrue(exception.getMessage().contains(TIMEZONE+" is not valid: invalid/invalid"));
    }

    private static Stream<Arguments> getInValidCalendarProperties() {

        return Stream.of(
                Arguments.of(Map.of(), List.of("Property " + START_HOUR+" is required", "Property " + END_HOUR+ " is required")),
                Arguments.of(Map.of(START_HOUR, "9"), List.of("Property " + END_HOUR+ " is required")),
                Arguments.of(Map.of(END_HOUR, "17"), List.of("Property " + START_HOUR+ " is required")),
                Arguments.of(Map.of(START_HOUR, "9", END_HOUR, "25"), List.of(END_HOUR+" 25 outside expected boundaries (0-24)")),
                Arguments.of(Map.of(START_HOUR, "26", END_HOUR, "-2"), List.of(START_HOUR+" 26 outside expected boundaries (0-24)", END_HOUR+" -2 outside expected boundaries (0-24)")),
                Arguments.of(Map.of(START_HOUR, "10", END_HOUR, "4", WEEKEND_DAYS, "1,2,8,9"), List.of(WEEKEND_DAYS+" [8, 9] outside expected boundaries (0-7)")),
                Arguments.of(Map.of(START_HOUR, "10", END_HOUR, "10"), List.of(START_HOUR+" 10 and "+END_HOUR+" 10 must be different")),
                Arguments.of(Map.of(START_HOUR, "10", END_HOUR, "4", WEEKEND_DAYS, "0,1,2"), List.of("0 (= no weekends) and other values provided in the given "+WEEKEND_DAYS+ " 0,1,2")),
                Arguments.of(Map.of(START_HOUR, "10", END_HOUR, "4", WEEKEND_DAYS, "1,1,2"), List.of("There are repeated values in the given "+WEEKEND_DAYS+" 1,1,2")),
                Arguments.of(Map.of(START_HOUR, "", END_HOUR, ""), List.of(START_HOUR+" is not valid: For input string: \"\"", END_HOUR+" is not valid: For input string: \"\"")));
    }
}