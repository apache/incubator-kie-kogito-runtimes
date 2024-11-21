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

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.kogito.timer.SessionPseudoClock;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.END_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAYS;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAY_DATE_FORMAT;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.START_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.TIMEZONE;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.WEEKEND_DAYS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void testCalculateHours() {
        Properties config = new Properties();
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");
        String expectedDate = "2012-05-04 16:45";
        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-04 13:45").getTime());

        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("3h");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateHoursCustomWorkingHours() {
        Properties config = new Properties();
        config.setProperty(START_HOUR, "11");
        config.setProperty(END_HOUR, "17");
        String expectedDate = "2012-05-04 15:45";

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-03 13:45").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("8h");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateHoursPassingOverWeekend() {
        Properties config = new Properties();
        String expectedDate = "2012-05-07 12:45";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-04 13:45").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("7h");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateHoursPassingOverCustomDefinedWeekend() {
        Properties config = new Properties();
        config.setProperty(WEEKEND_DAYS, Calendar.FRIDAY + "," + Calendar.SATURDAY);
        String expectedDate = "2012-05-06 12:45";
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-03 13:45").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("7h");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateMinutesPassingOverWeekend() {
        Properties config = new Properties();
        String expectedDate = "2012-05-07 09:15";
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-04 16:45").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("30m");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateMinutesPassingOverHoliday() {
        Properties config = new Properties();
        config.setProperty(BusinessCalendarImpl.HOLIDAYS, "2012-05-12:2012-05-19");
        String expectedDate = "2012-05-21 09:15";
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-11 16:45").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("30m");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateDays() {
        Properties config = new Properties();
        String expectedDate = "2012-05-14 09:00";
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDate("2012-05-04").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("6d");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateDaysStartingInWeekend() {
        Properties config = new Properties();
        String expectedDate = "2012-05-09 09:00";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDate("2012-05-05").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("2d");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateDaysCustomWorkingDays() {
        Properties config = new Properties();
        config.setProperty(WEEKEND_DAYS, Calendar.FRIDAY + "," + Calendar.SATURDAY + "," + Calendar.SUNDAY);
        String expectedDate = "2012-05-15 14:30";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-03 14:30").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("6d");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateDaysMiddleDay() {
        Properties config = new Properties();
        String expectedDate = "2012-05-11 12:27";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-03 12:27").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("6d");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateDaysHoursMinutes() {
        Properties config = new Properties();
        String expectedDate = "2012-05-14 14:20";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDate("2012-05-04").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("6d4h80m");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateTimeDaysHoursMinutesHolidays() {
        Properties config = new Properties();
        config.setProperty(BusinessCalendarImpl.HOLIDAYS, "2012-05-10:2012-05-19");
        String expectedDate = "2012-05-21 14:20";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDate("2012-05-04").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("6d4h80m");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateTimeDaysHoursMinutesSingleDayHolidays() {
        Properties config = new Properties();
        config.setProperty(BusinessCalendarImpl.HOLIDAYS, "2012-05-07");
        String expectedDate = "2012-05-08 13:20";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDate("2012-05-04").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("1d4h20m");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateTimeDaysHoursMinutesSingleDayHolidaysInMiddleOfWeek() {
        Properties config = new Properties();
        config.setProperty(BusinessCalendarImpl.HOLIDAYS, "2012-05-09");
        String expectedDate = "2012-05-10 15:30";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-08 11:10").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("1d4h20m");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateDaysPassingOverHolidayAtYearEnd() {
        Properties config = new Properties();
        config.setProperty(BusinessCalendarImpl.HOLIDAYS, "2012-12-31:2013-01-01");
        String expectedDate = "2013-01-04 09:15";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-12-28 16:45").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("2d30m");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateDaysPassingOverHolidayAtYearEndWithWildcards() {
        Properties config = new Properties();
        config.setProperty(BusinessCalendarImpl.HOLIDAYS, "*-12-31:*-01-01");
        String expectedDate = "2013-01-02 09:15";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-12-28 16:45").getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("2d30m");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateISOHours() {
        Properties config = new Properties();
        String expectedDate = "2012-05-04 16:45";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-04 13:45").getTime());

        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("PT3H");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateISODaysAndHours() {
        Properties config = new Properties();
        config.setProperty(BusinessCalendarImpl.HOLIDAYS, "2012-05-09");
        String expectedDate = "2012-05-10 15:30";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime("2012-05-08 11:10").getTime());

        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("P1DT4H20M");

        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testSingleHolidayWithinGivenTime() {
        final Properties props = new Properties();
        props.put(BusinessCalendarImpl.HOLIDAYS, "2015-01-13");
        String expectedDate = "2015-01-15 11:38";

        props.setProperty(START_HOUR, "9");
        props.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTimeAndMillis("2015-01-08 11:38:30.198").getTime());

        BusinessCalendarImpl businessCalendarImpl = new BusinessCalendarImpl(props, clock);

        Date result = businessCalendarImpl.calculateBusinessTimeAsDate("4d");
        assertThat(formatDate("yyyy-MM-dd HH:mm", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateMillisecondsAsDefault() {
        Properties config = new Properties();
        String expectedDate = "2012-05-04 16:45:10.000";
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");
        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTimeAndMillis("2012-05-04 16:45:00.000").getTime());

        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("10000");

        assertThat(formatDate("yyyy-MM-dd HH:mm:ss.SSS", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateMinutesPassingAfterHour() {
        Properties config = new Properties();
        String currentDate = "2018-05-02 19:51:33";
        String expectedDate = "2018-05-03 09:01:00";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime(currentDate).getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate("1m");

        assertThat(formatDate("yyyy-MM-dd HH:mm:ss", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testBusinessCalendarWithoutProvidedConfiguration() {
        Properties config = new Properties();
        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");
        assertDoesNotThrow(() -> new BusinessCalendarImpl(config));
    }

    @Test
    public void testCalculateMinutesPassingHoliday() {
        Properties config = new Properties();
        config.setProperty(BusinessCalendarImpl.START_HOUR, "9");
        config.setProperty(BusinessCalendarImpl.END_HOUR, "18");
        config.setProperty(WEEKEND_DAYS, "1,7"); // sun,sat
        config.setProperty(BusinessCalendarImpl.HOLIDAYS, "2018-04-30,2018-05-03:2018-05-05");
        config.setProperty(BusinessCalendarImpl.HOLIDAY_DATE_FORMAT, "yyyy-MM-dd");
        String currentDate = "2018-05-03 13:51:33";
        String duration = "10m";
        String expectedDate = "2018-05-07 09:10:00";

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime(currentDate).getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate(duration);

        assertThat(formatDate("yyyy-MM-dd HH:mm:ss", result)).isEqualTo(expectedDate);
    }

    @Test
    public void testCalculateMinutesPassingWeekend() {
        Properties config = new Properties();
        String currentDate = "2018-05-06 13:51:33";
        String duration = "10m";
        String expectedDate = "2018-05-07 09:10:00";

        config.setProperty(START_HOUR, "9");
        config.setProperty(END_HOUR, "17");

        SessionPseudoClock clock = new StaticPseudoClock(parseToDateWithTime(currentDate).getTime());
        BusinessCalendarImpl businessCal = new BusinessCalendarImpl(config, clock);

        Date result = businessCal.calculateBusinessTimeAsDate(duration);

        assertThat(formatDate("yyyy-MM-dd HH:mm:ss", result)).isEqualTo(expectedDate);
    }

    @ParameterizedTest
    @MethodSource("getValidCalendarProperties")
    public void testValidationForValidProperties(Map<String, Object> propertyMap, Map<String, Object> expectedValuesMap) throws NoSuchFieldException, IllegalAccessException {
        Properties businessCalendarProperties = new Properties();
        businessCalendarProperties.putAll(propertyMap);
        List<BusinessCalendarImpl> businessCalendarList = new ArrayList<>();
        assertDoesNotThrow(() -> {
            businessCalendarList.add(new BusinessCalendarImpl(businessCalendarProperties));
        });
        assertCalendarProperties(businessCalendarList.get(0), expectedValuesMap);
    }

    @ParameterizedTest
    @MethodSource("getInValidCalendarProperties")
    public void testValidationForInvalidProperties(Map<String, Object> propertyMap, List<String> errorMessages) throws NoSuchFieldException, IllegalAccessException {
        Properties businessCalendarProperties = new Properties();
        businessCalendarProperties.putAll(propertyMap);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new BusinessCalendarImpl(businessCalendarProperties);
        });
        errorMessages.forEach(msg -> assertTrue(exception.getMessage().contains(msg)));
    }

    @Test
    public void testValidationForInvalidFormats() {
        Map<String, Object> propertyMap = Map.of(START_HOUR, "2", END_HOUR, "12", HOLIDAY_DATE_FORMAT, "aaa/y/d", HOLIDAYS,
                "22-12-121", TIMEZONE, "invalid/invalid");
        Properties businessCalendarProperties = new Properties();
        businessCalendarProperties.putAll(propertyMap);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new BusinessCalendarImpl(businessCalendarProperties);
        });
        assertTrue(exception.getMessage().contains("Invalid holidays"));
        assertTrue(exception.getMessage().contains("Invalid timezone"));
    }

    private Date parseToDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date testTime;
        try {
            testTime = sdf.parse(dateString);

            return testTime;
        } catch (ParseException e) {
            return null;
        }
    }

    private Date parseToDateWithTime(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date testTime;
        try {
            testTime = sdf.parse(dateString);

            return testTime;
        } catch (ParseException e) {
            return null;
        }
    }

    private Date parseToDateWithTimeAndMillis(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Date testTime;
        try {
            testTime = sdf.parse(dateString);

            return testTime;
        } catch (ParseException e) {
            return null;
        }
    }

    private String formatDate(String pattern, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        String testTime = sdf.format(date);

        return testTime;

    }

    private class StaticPseudoClock implements SessionPseudoClock {

        private long currentTime;

        private StaticPseudoClock(long currenttime) {
            this.currentTime = currenttime;
        }

        public long getCurrentTime() {
            return this.currentTime;
        }

        public long advanceTime(long amount, TimeUnit unit) {
            throw new UnsupportedOperationException("It is static clock and does not allow advance time operation");
        }

    }

    private static Stream<Arguments> getValidCalendarProperties() {

        return Stream.of(
                Arguments.of(Map.of(START_HOUR, "9", END_HOUR, "17"),
                        Map.of(WEEKEND_DAY_FIELD, List.of(7, 1), START_HOUR_FIELD, 9, END_HOUR_FIELD, 17, HOURS_IN_DAY_FIELD, 8, DAYS_PER_WEEK_FIELD, 5)),
                Arguments.of(Map.of(WEEKEND_DAYS, "1, 2, 3", START_HOUR, "21", END_HOUR, "7"),
                        Map.of(WEEKEND_DAY_FIELD, List.of(1, 2, 3), START_HOUR_FIELD, 21, END_HOUR_FIELD, 7, HOURS_IN_DAY_FIELD, 10, DAYS_PER_WEEK_FIELD, 4)));
    }

    private static Stream<Arguments> getInValidCalendarProperties() {

        return Stream.of(
                Arguments.of(Map.of(), List.of("Property required: " + START_HOUR, "Property required: " + END_HOUR)),
                Arguments.of(Map.of(START_HOUR, "9"), List.of("Property required: " + END_HOUR)),
                Arguments.of(Map.of(END_HOUR, "17"), List.of("Property required: " + START_HOUR)),
                Arguments.of(Map.of(START_HOUR, "9", END_HOUR, "24"), List.of("Invalid property: " + END_HOUR)),
                Arguments.of(Map.of(START_HOUR, "24", END_HOUR, "24"), List.of("Invalid property: " + START_HOUR, "Invalid property: " + END_HOUR)),
                Arguments.of(Map.of(START_HOUR, "10", END_HOUR, "4", WEEKEND_DAYS, "1,2,8"), List.of("Invalid property: " + WEEKEND_DAYS)),
                Arguments.of(Map.of(START_HOUR, "", END_HOUR, ""), List.of("Property is not a number: " + START_HOUR, "Property is not a number: " + END_HOUR)));
    }

    private void assertCalendarProperties(BusinessCalendarImpl businessCalendar, Map<String, Object> expectedValuesMap) throws NoSuchFieldException, IllegalAccessException {
        Field daysPerWeekField = BusinessCalendarImpl.class.getDeclaredField(DAYS_PER_WEEK_FIELD);
        daysPerWeekField.setAccessible(true);
        Field startHourField = BusinessCalendarImpl.class.getDeclaredField(START_HOUR_FIELD);
        startHourField.setAccessible(true);
        Field endHourField = BusinessCalendarImpl.class.getDeclaredField(END_HOUR_FIELD);
        endHourField.setAccessible(true);
        Field hoursInDayField = BusinessCalendarImpl.class.getDeclaredField(HOURS_IN_DAY_FIELD);
        hoursInDayField.setAccessible(true);
        Field weekendDaysField = BusinessCalendarImpl.class.getDeclaredField(WEEKEND_DAY_FIELD);
        weekendDaysField.setAccessible(true);

        assertEquals(expectedValuesMap.get(START_HOUR_FIELD), startHourField.get(businessCalendar));
        assertEquals(expectedValuesMap.get(END_HOUR_FIELD), endHourField.get(businessCalendar));
        assertEquals(expectedValuesMap.get(DAYS_PER_WEEK_FIELD), daysPerWeekField.get(businessCalendar));
        assertEquals(expectedValuesMap.get(HOURS_IN_DAY_FIELD), hoursInDayField.get(businessCalendar));
        assertEquals(expectedValuesMap.get(WEEKEND_DAY_FIELD), weekendDaysField.get(businessCalendar));

    }
}
