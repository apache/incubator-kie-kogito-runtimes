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

import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.END_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.START_HOUR;
import static org.junit.jupiter.api.Assertions.*;

class CalendarBeanTest {

    @Test
    void requiredValidationMissingEndHour() {
        Properties config = new Properties();
        config.setProperty(START_HOUR, "25");
        StringBuilder errors = new StringBuilder();
        assertThat(errors).isEmpty();
        CalendarBean.requiredValidation(errors, config);
        assertThat(errors).isNotEmpty();
        String expected = "Start hour 25 outside expected boundaries (0-24)";
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
        String expected = "Start hour 25 outside expected boundaries (0-24)";
        assertThat(errors).contains(expected);

    }
}