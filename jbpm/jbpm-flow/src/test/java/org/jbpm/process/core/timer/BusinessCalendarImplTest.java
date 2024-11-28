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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.END_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAYS;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAY_DATE_FORMAT;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.START_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.WEEKEND_DAYS;
import static org.jbpm.process.core.timer.CalendarBean.DEFAULT_HOLIDAY_DATE_FORMAT;

public class BusinessCalendarImplTest extends AbstractBaseTest {

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

    // TODO
    // name this testing method after the method that is actually tested
    @Test
    public void calculateBusinessTimeAsDateINsideWorkingHOur() {
        // TODO revert the test logic.
        // Instead of if evaluation, calcuated start and end hour based on current time,
        // and make proper assertion
        // Do not create the "functionally" same property in multiple times (e.g. "9" used in configuration is exactly the same 9 used in LocalTime.of

        String dateTimeFormat = "yyyy-MM-dd HH:mm";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateTimeFormat);
        LocalDateTime currentTime = LocalDateTime.now();
        int triggerDelay = 3;
        LocalDateTime firstTriggerTime = currentTime.plusHours(triggerDelay);
        LocalDateTime startTime = firstTriggerTime.minusHours(4);
        LocalDateTime endTime = firstTriggerTime.plusHours(4);

        int startHour = startTime.getHour();
        int endHour = endTime.getHour();

        Properties config = new Properties();
        config.setProperty(START_HOUR, String.valueOf(startHour));
        config.setProperty(END_HOUR, String.valueOf(endHour));
        config.setProperty(WEEKEND_DAYS, "0");

        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();
        Date retrieved = businessCal.calculateBusinessTimeAsDate(String.format("%sh", triggerDelay));
        Instant resultInstant = retrieved.toInstant();
        Instant firstTriggerInstant = firstTriggerTime.toInstant(ZoneOffset.of("Z"));
        assertThat(firstTriggerInstant).isEqualTo(resultInstant);

        // modify parameters and repeat test to verify all the conditions written in the if/else statements

        //        if (firstTriggerTime.isAfter(startTime) && firstTriggerTime.isBefore(endTime)) {
        //            assertThat(formatDate(dateTimeFormat, result)).isEqualTo(firstTriggerTime.format(dtf));
        //        } else if (currentTime.isBefore(startTime)) {
        //            LocalTime actualTriggerTime = currentTime;
        //
        //            while (actualTriggerTime.isBefore(startTime)) {
        //                actualTriggerTime = actualTriggerTime.plusHours(3);
        //            }
        //            assertThat(formatDate(dateTimeFormat, result)).isEqualTo(actualTriggerTime.plusHours(3).atDate(LocalDate.now()).format(dtf));
        //        } else {
        //            LocalDateTime actualTriggerTime = startTime.plusHours(3).atDate(LocalDate.now().plusDays(1));
        //            assertThat(formatDate(dateTimeFormat, result)).isEqualTo(actualTriggerTime.format(dtf));
        //        }
    }

    @Test
    public void testCalculateHoursWhenTodayAndTomorrowAreHolidays() {
        int startHour = 9;
        Properties config = new Properties();
        config.setProperty(START_HOUR, String.valueOf(startHour));
        config.setProperty(END_HOUR, "17");
        config.setProperty(WEEKEND_DAYS, "0");
        String dateFormat = "yyyy-MM-dd";
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(dateFormat);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        config.setProperty(HOLIDAY_DATE_FORMAT, dateFormat);
        config.setProperty(HOLIDAYS, sdf.format(today) + "," + sdf.format(tomorrow));

        String dateTimeFormat = "yyyy-MM-dd HH:mm";

        LocalTime startTime = LocalTime.of(startHour, 0);
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

    @Test
    void rollCalendarAfterHolidays() {
        Instant now = Instant.now();
        int holidayLeft = 4;
        Instant startHolidayInstant = now.minus(2, DAYS);
        Instant endHolidayInstant = now.plus(holidayLeft, DAYS);
        Date startHoliday = Date.from(startHolidayInstant);
        Date endHoliday = Date.from(endHolidayInstant);
        String startHolidayFormatted = formatDate(DEFAULT_HOLIDAY_DATE_FORMAT, startHoliday);
        String endHolidayFormatted = formatDate(DEFAULT_HOLIDAY_DATE_FORMAT, endHoliday);
        String holidays = String.format("%s:%s", startHolidayFormatted, endHolidayFormatted);
        Properties config = new Properties();
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");
        config.setProperty(WEEKEND_DAYS, "0");
        config.setProperty(HOLIDAYS, holidays);
        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();

        Calendar calendar = Calendar.getInstance();
        int currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        businessCal.rollCalendarAfterHolidays(calendar, false);
        int expected = currentDayOfYear + holidayLeft + 1;
        assertThat(calendar.get(Calendar.DAY_OF_YEAR)).isEqualTo(expected);
    }

    @Test
    void rollCalendarToNextWorkingDay() {
        List<Integer> workingDays = IntStream.range(Calendar.MONDAY, Calendar.SATURDAY).boxed().toList();
        List<Integer> weekendDays = Arrays.asList(Calendar.SATURDAY, Calendar.SUNDAY);
        Properties config = new Properties();
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");
        config.setProperty(WEEKEND_DAYS, weekendDays.stream().map(String::valueOf).collect(Collectors.joining(",")));
        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();

        workingDays.forEach(workingDay -> {
            Calendar calendar = getCalendarAtExpectedWeekDay(workingDay);
            businessCal.rollCalendarToNextWorkingDay(calendar, false);
            assertThat(calendar.get(Calendar.DAY_OF_WEEK)).isEqualTo(workingDay);
        });
        weekendDays.forEach(weekendDay -> {
            Calendar calendar = getCalendarAtExpectedWeekDay(weekendDay);
            businessCal.rollCalendarToNextWorkingDay(calendar, false);
            assertThat(calendar.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.MONDAY);
        });
    }

    @Test
    void isWorkingDay() {
        List<Integer> workingDays = IntStream.range(Calendar.MONDAY, Calendar.SATURDAY).boxed().toList();
        List<Integer> weekendDays = Arrays.asList(Calendar.SATURDAY, Calendar.SUNDAY);
        Properties config = new Properties();
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");
        config.setProperty(WEEKEND_DAYS, weekendDays.stream().map(String::valueOf).collect(Collectors.joining(",")));
        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();
        workingDays.forEach(workingDay -> assertThat(businessCal.isWorkingDay(workingDay)).isTrue());
        weekendDays.forEach(workingDay -> assertThat(businessCal.isWorkingDay(workingDay)).isFalse());
    }

    private Calendar getCalendarAtExpectedWeekDay(int weekDay) {
        Calendar toReturn = Calendar.getInstance();
        while (toReturn.get(Calendar.DAY_OF_WEEK) != weekDay) {
            toReturn.add(Calendar.DAY_OF_YEAR, 1);
        }
        return toReturn;
    }

    private String formatDate(String pattern, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    private String formatLocalDate(String pattern, LocalDate date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
}
