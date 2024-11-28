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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.END_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAYS;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAY_DATE_FORMAT;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.START_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.WEEKEND_DAYS;

public class BusinessCalendarImplTest extends AbstractBaseTest {

    private static final String START_HOUR_FIELD = "startHour";
    private static final String END_HOUR_FIELD = "endHour";
    private static final String HOURS_IN_DAY_FIELD = "hoursInDay";
    private static final String WEEKEND_DAY_FIELD = "weekendDays";
    private static final String DAYS_PER_WEEK_FIELD = "daysPerWeek";

    public void addLogger() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Test
    void instantiate() {
        BusinessCalendarImpl retrieved = BusinessCalendarImpl.builder().build();
        assertThat(retrieved).isNotNull();
        retrieved = BusinessCalendarImpl.builder()
                .withCalendarBean(CalendarBeanFactory.createCalendarBean())
                .build();
        assertThat(retrieved).isNotNull();

        Properties calendarConfiguration = new Properties();
        int startHour = 10;
        int endHour = 16;
        calendarConfiguration.put(START_HOUR, String.valueOf(startHour));
        calendarConfiguration.put(END_HOUR, String.valueOf(endHour));
        retrieved = BusinessCalendarImpl.builder()
                .withCalendarBean(new CalendarBean(calendarConfiguration))
                .build();
        assertThat(retrieved).isNotNull();
    }

    @Test
    public void testCalculateHours() {
        Properties config = new Properties();
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");
        config.setProperty(WEEKEND_DAYS, "0");

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        LocalTime currentTime = LocalTime.now();
        LocalTime firstTriggerTime = currentTime.plusHours(3);

        String dateTimeFormat = "yyyy-MM-dd HH:mm";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateTimeFormat);

        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();

        Date result = businessCal.calculateBusinessTimeAsDate("3h");

        if (firstTriggerTime.isAfter(startTime) && firstTriggerTime.isBefore(endTime)) {
            assertThat(formatDate(dateTimeFormat, result)).isEqualTo(firstTriggerTime.format(dtf));
        } else if (currentTime.isBefore(startTime)) {
            LocalTime actualTriggerTime = currentTime;

            while (actualTriggerTime.isBefore(startTime)) {
                actualTriggerTime = actualTriggerTime.plusHours(3);
            }
            assertThat(formatDate(dateTimeFormat, result)).isEqualTo(actualTriggerTime.plusHours(3).atDate(LocalDate.now()).format(dtf));
        } else {
            LocalDateTime actualTriggerTime = startTime.plusHours(3).atDate(LocalDate.now().plusDays(1));
            assertThat(formatDate(dateTimeFormat, result)).isEqualTo(actualTriggerTime.format(dtf));
        }
    }

    @Test
    public void testCalculateHoursWhenTodayAndTomorrowAreHolidays() {
        Properties config = new Properties();
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");
        config.setProperty(WEEKEND_DAYS, "0");
        String dateFormat = "yyyy-MM-dd";
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(dateFormat);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        config.setProperty(HOLIDAY_DATE_FORMAT, dateFormat);
        config.setProperty(HOLIDAYS, sdf.format(today) + "," + sdf.format(tomorrow));

        String dateTimeFormat = "yyyy-MM-dd HH:mm";

        LocalTime startTime = LocalTime.of(9, 0);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateTimeFormat);

        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();

        Date result = businessCal.calculateBusinessTimeAsDate("3h");
        LocalTime actualTriggerTime = LocalTime.now().isBefore(startTime) ? LocalTime.now() : startTime;

        while (actualTriggerTime.isBefore(startTime)) {
            actualTriggerTime = actualTriggerTime.plusHours(3);
        }
        actualTriggerTime = actualTriggerTime.plusHours(3);

        assertThat(formatDate(dateTimeFormat, result)).isEqualTo(actualTriggerTime.atDate(LocalDate.now().plusDays(2)).format(dtf));
    }

    @Test
    public void testCalculateHoursWhenNextDayIsHoliday() {
        Properties config = new Properties();
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");
        config.setProperty(WEEKEND_DAYS, "0");
        String dateFormat = "yyyy-MM-dd";
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(dateFormat);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        config.setProperty(HOLIDAY_DATE_FORMAT, dateFormat);
        config.setProperty(HOLIDAYS, sdf.format(tomorrow));

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        LocalTime currentTime = LocalTime.now();
        LocalTime firstTriggerTime = currentTime.plusHours(3);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();

        Date result = businessCal.calculateBusinessTimeAsDate("3h");

        String dateTimeFormat = "yyyy-MM-dd HH:mm";

        if (firstTriggerTime.isAfter(startTime) && firstTriggerTime.isBefore(endTime)) {
            assertThat(formatDate(dateTimeFormat, result)).isEqualTo(firstTriggerTime.format(dtf));
        } else if (currentTime.isBefore(startTime)) {
            LocalTime actualTriggerTime = currentTime;

            while (actualTriggerTime.isBefore(startTime)) {
                actualTriggerTime = actualTriggerTime.plusHours(3);
            }
            assertThat(formatDate(dateTimeFormat, result)).isEqualTo(actualTriggerTime.plusHours(3).atDate(LocalDate.now()).format(dtf));
        } else {
            LocalDateTime actualTriggerTime = startTime.plusHours(3).atDate(LocalDate.now().plusDays(2));
            assertThat(formatDate(dateTimeFormat, result)).isEqualTo(actualTriggerTime.format(dtf));
        }
    }

    private String formatDate(String pattern, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        String testTime = sdf.format(date);

        return testTime;

    }
}
