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
import java.util.function.BiFunction;
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
    public void calculateBusinessTimeAsDateInsideDailyWorkingHour() {
        commonCalculateBusinessTimeAsDateAssertBetweenHours(-4, 4, 3, 0, null, null);
//        //executed at 10.48
//        //first trigger time:2024-11-29T01:48:01.955975900
//        //start time: 2024-11-28T21:48:01.955975900
//        //end hour: 2024-11-29T05:48:01.955975900
//        //expected trigger time is between start time and end time
//        //but actual is Mon Dec 02 13:00:00 EST 2024(after 2 days)
//        assertTrue(resultInstant.isAfter(startTime.toInstant(ZoneOffset.of("Z"))));
//        assertTrue(resultInstant.isBefore(endTime.toInstant(ZoneOffset.of("Z"))));
    }

    @Test
    public void calculateBusinessTimeAsDateInsideNightlyWorkingHour() {
        commonCalculateBusinessTimeAsDateAssertBetweenHours(4, -4, 3, 0, null, null);
        //        //executed at 10.48
        //        //first trigger time:2024-11-29T01:48:01.955975900
        //        //start time: 2024-11-28T21:48:01.955975900
        //        //end hour: 2024-11-29T05:48:01.955975900
        //        //expected trigger time is between start time and end time
        //        //but actual is Mon Dec 02 13:00:00 EST 2024(after 2 days)
        //        assertTrue(resultInstant.isAfter(startTime.toInstant(ZoneOffset.of("Z"))));
        //        assertTrue(resultInstant.isBefore(endTime.toInstant(ZoneOffset.of("Z"))));
    }

    @Test
    public void calculateBusinessTimeAsDateBeforeWorkingHour() {
        commonCalculateBusinessTimeAsDateAssertAtStartHour(2, 4, 1, 0, null, null);
    }

    @Test
    public void calculateBusinessTimeAsDateWhenTodayAndTomorrowAreHolidays() {
        String holidayDateFormat = "yyyy-MM-dd";
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(holidayDateFormat);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        String holidays = sdf.format(today) + "," + sdf.format(tomorrow);
        commonCalculateBusinessTimeAsDateAssertBetweenHours(-4, 4, 3, 2, holidayDateFormat, holidays);
    }

    @Test
    public void calculateBusinessTimeAsDateWhenNextDayIsHoliday() {
        String holidayDateFormat = "yyyy-MM-dd";
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(holidayDateFormat);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String holidays = sdf.format(tomorrow);
        commonCalculateBusinessTimeAsDateAssertBetweenHours(-4, 4, 3, 0, holidayDateFormat, holidays);
    }

    @Test
    void rollCalendarToDailyWorkingHour() {
        int startHour = 14;
        int endHour = 16;
        Calendar toRoll = Calendar.getInstance();
        int currentHour = 8;
        toRoll.set(Calendar.HOUR_OF_DAY, currentHour);
        int dayOfYear = toRoll.get(Calendar.DAY_OF_YEAR);
        BusinessCalendarImpl.rollCalendarToDailyWorkingHour(toRoll, startHour, endHour);
        assertThat(toRoll.get(Calendar.HOUR_OF_DAY)).isEqualTo(startHour);
        assertThat(toRoll.get(Calendar.DAY_OF_YEAR)).isEqualTo(dayOfYear);

        toRoll = Calendar.getInstance();
        currentHour = 19;
        toRoll.set(Calendar.HOUR_OF_DAY, currentHour);
        dayOfYear = toRoll.get(Calendar.DAY_OF_YEAR);
        BusinessCalendarImpl.rollCalendarToDailyWorkingHour(toRoll, startHour, endHour);
        assertThat(toRoll.get(Calendar.HOUR_OF_DAY)).isEqualTo(startHour);
        assertThat(toRoll.get(Calendar.DAY_OF_YEAR)).isEqualTo(dayOfYear + 1);
    }

    @Test
    void rollCalendarToNightlyWorkingHour() {
        int startHour = 20;
        int endHour = 4;
        Calendar toRoll = Calendar.getInstance();
        int currentHour = 21;
        toRoll.set(Calendar.HOUR_OF_DAY, currentHour);
        int dayOfYear = toRoll.get(Calendar.DAY_OF_YEAR);
        BusinessCalendarImpl.rollCalendarToNightlyWorkingHour(toRoll, startHour, endHour);
        assertThat(toRoll.get(Calendar.HOUR_OF_DAY)).isEqualTo(startHour);
        assertThat(toRoll.get(Calendar.DAY_OF_YEAR)).isEqualTo(dayOfYear);

        toRoll = Calendar.getInstance();
        currentHour = 3;
        toRoll.set(Calendar.HOUR_OF_DAY, currentHour);
        dayOfYear = toRoll.get(Calendar.DAY_OF_YEAR);
        BusinessCalendarImpl.rollCalendarToNightlyWorkingHour(toRoll, startHour, endHour);
        assertThat(toRoll.get(Calendar.HOUR_OF_DAY)).isEqualTo(startHour);
        assertThat(toRoll.get(Calendar.DAY_OF_YEAR)).isEqualTo(dayOfYear + 1);

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

    private void commonCalculateBusinessTimeAsDateAssertBetweenHours(int startHourGap, int endHourGap, int hourDelay, int numberOfHolidays, String holidayDateFormat, String holidays ) {
        BiFunction<Instant, Instant, Boolean> startBooleanCondition = (resultInstant1, expectedStartTime1) -> resultInstant1.isAfter(expectedStartTime1);
        commonCalculateBusinessTimeAsDate(startHourGap,
                endHourGap,
                hourDelay,
                numberOfHolidays,
                holidayDateFormat,
                holidays,
                startBooleanCondition);
    }

    private void commonCalculateBusinessTimeAsDateAssertAtStartHour(int startHourGap, int endHourGap, int hourDelay, int numberOfHolidays, String holidayDateFormat, String holidays ) {
        BiFunction<Instant, Instant, Boolean> startBooleanCondition = (resultInstant, expectedStartTime) -> resultInstant.getEpochSecond() ==  expectedStartTime.getEpochSecond();
        commonCalculateBusinessTimeAsDate(startHourGap,
                endHourGap,
                hourDelay,
                numberOfHolidays,
                holidayDateFormat,
                holidays,
                startBooleanCondition);
    }

    private void commonCalculateBusinessTimeAsDate(int startHourGap,
            int endHourGap, int hourDelay, int numberOfHolidays, String holidayDateFormat, String holidays,
            BiFunction<Instant, Instant, Boolean> startBooleanCondition) {
        LocalDateTime currentTime = LocalDateTime.now();
        if (hourDelay != 0) {
            currentTime = currentTime.plusHours(hourDelay);
        }
        LocalDateTime correctedTIme = LocalDateTime.of(currentTime.getYear(), currentTime.getMonthValue(), currentTime.getDayOfMonth(), currentTime.getHour(), 0);
        LocalDateTime startTime = correctedTIme.plusHours(startHourGap);
        LocalDateTime endTime = correctedTIme.plusHours(endHourGap);

        int startHour = startTime.getHour();
        int endHour = endTime.getHour();

        Properties config = new Properties();
        config.setProperty(START_HOUR, String.valueOf(startHour));
        config.setProperty(END_HOUR, String.valueOf(endHour));
        config.setProperty(WEEKEND_DAYS, "0");
        if (holidayDateFormat != null) {
            config.setProperty(HOLIDAY_DATE_FORMAT, holidayDateFormat);
        }
        if (holidays != null) {
            config.setProperty(HOLIDAYS, holidays);
        }

        BusinessCalendarImpl businessCal = BusinessCalendarImpl.builder().withCalendarBean(new CalendarBean(config)).build();
        Date retrieved = businessCal.calculateBusinessTimeAsDate(String.format("%sh", hourDelay));



        Instant resultInstant = retrieved.toInstant();

        Instant expectedStartTime = numberOfHolidays > 0 ? startTime.plusDays(numberOfHolidays).toInstant(ZoneOffset.of("Z")) : startTime.toInstant(ZoneOffset.of("Z"));
        Instant expectedEndTime = numberOfHolidays > 0 ? endTime.plusDays(numberOfHolidays).toInstant(ZoneOffset.of("Z")) : endTime.toInstant(ZoneOffset.of("Z"));

        System.out.println("resultInstant " + resultInstant.getEpochSecond());
        System.out.println("expectedStartTime " + expectedStartTime.getEpochSecond());
        System.out.println("expectedEndTime " + expectedEndTime.getEpochSecond());

        assertTrue(startBooleanCondition.apply(resultInstant, expectedStartTime));
        assertTrue(resultInstant.isBefore(expectedEndTime));
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
}
