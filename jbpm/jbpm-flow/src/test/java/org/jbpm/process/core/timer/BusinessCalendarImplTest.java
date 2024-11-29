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

import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.END_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAYS;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAY_DATE_FORMAT;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.START_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.WEEKEND_DAYS;
import static org.jbpm.process.core.timer.CalendarBean.DEFAULT_HOLIDAY_DATE_FORMAT;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void calculateBusinessTimeAsDateInsideWorkingHour() {

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
        //executed at 10.48
        //first trigger time:2024-11-29T01:48:01.955975900
        //start time: 2024-11-28T21:48:01.955975900
        //end hour: 2024-11-29T05:48:01.955975900
        //expected trigger time is between start time and end time
        //but actual is Mon Dec 02 13:00:00 EST 2024(after 2 days)
        assertTrue(resultInstant.isAfter(startTime.toInstant(ZoneOffset.of("Z"))));
        assertTrue(resultInstant.isBefore(endTime.toInstant(ZoneOffset.of("Z"))));
    }

    @Test
    public void calculateBusinessTimeAsDateWhenTodayAndTomorrowAreHolidays() {
        Properties config = new Properties();
        String dateFormat = "yyyy-MM-dd";
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(dateFormat);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        int triggerDelay = 3;
        int numberOfHolidays = 2;
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime firstTriggerTime = currentTime.plusHours(triggerDelay);
        LocalDateTime startTime = firstTriggerTime.minusHours(4);
        LocalDateTime endTime = firstTriggerTime.plusHours(4);

        int startHour = startTime.getHour();
        int endHour = endTime.getHour();

        config.setProperty(START_HOUR, String.valueOf(startHour));
        config.setProperty(END_HOUR, String.valueOf(endHour));
        config.setProperty(WEEKEND_DAYS, "0");

        config.setProperty(HOLIDAY_DATE_FORMAT, dateFormat);
        config.setProperty(HOLIDAYS, sdf.format(today) + "," + sdf.format(tomorrow));

        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();
        Date retrieved = businessCal.calculateBusinessTimeAsDate(String.format("%sh", triggerDelay));
        Instant resultInstant = retrieved.toInstant();

        assertTrue(resultInstant.isAfter(startTime.plusDays(numberOfHolidays).toInstant(ZoneOffset.of("Z"))));
        assertTrue(resultInstant.isBefore(endTime.plusDays(numberOfHolidays).toInstant(ZoneOffset.of("Z"))));

    }

    @Test
    public void calculateBusinessTimeAsDateWhenNextDayIsHoliday() {
        Properties config = new Properties();
        String dateFormat = "yyyy-MM-dd";
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(dateFormat);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        config.setProperty(HOLIDAY_DATE_FORMAT, dateFormat);
        config.setProperty(HOLIDAYS, sdf.format(tomorrow));

        int triggerDelay = 3;
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime firstTriggerTime = currentTime.plusHours(triggerDelay);
        LocalDateTime startTime = firstTriggerTime.minusHours(4);
        LocalDateTime endTime = firstTriggerTime.plusHours(4);

        int startHour = startTime.getHour();
        int endHour = endTime.getHour();

        config.setProperty(START_HOUR, String.valueOf(startHour));
        config.setProperty(END_HOUR, String.valueOf(endHour));
        config.setProperty(WEEKEND_DAYS, "0");

        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();
        Date retrieved = businessCal.calculateBusinessTimeAsDate(String.format("%sh", triggerDelay));
        Instant resultInstant = retrieved.toInstant();

        assertTrue(resultInstant.isAfter(startTime.toInstant(ZoneOffset.of("Z"))));
        assertTrue(resultInstant.isBefore(endTime.toInstant(ZoneOffset.of("Z"))));
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
