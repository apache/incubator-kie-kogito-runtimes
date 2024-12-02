/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jbpm.process.core.timer;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalendarBeanFactoryTest {

    @Test
    void testCreateCalendarBean() {
        // This test relies on src/test/resources/calendar.properties:
        // checked values comes from it
        CalendarBean calendarBean = CalendarBeanFactory.createCalendarBean();
        assertNotNull(calendarBean);
        assertEquals(10, calendarBean.getStartHour());
        assertEquals(16, calendarBean.getEndHour());
        assertEquals(6, calendarBean.getHoursInDay());
        assertEquals(5, calendarBean.getDaysPerWeek());
        assertEquals(List.of(Calendar.SATURDAY, Calendar.SUNDAY), calendarBean.getWeekendDays());
        assertEquals(List.of(), calendarBean.getHolidays());
        assertEquals(TimeZone.getDefault().getID(), calendarBean.getTimezone());
    }
}
