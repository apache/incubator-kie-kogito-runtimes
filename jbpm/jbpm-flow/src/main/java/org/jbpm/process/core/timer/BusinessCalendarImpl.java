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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.jbpm.util.PatternConstants;
import org.kie.kogito.calendar.BusinessCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.process.core.constants.CalendarConstants.BUSINESS_CALENDAR_PATH;

/**
 * Default implementation of BusinessCalendar interface that is configured with properties.
 * Following are supported properties:
 * <ul>
 * <li>business.start.hour - specifies starting hour of work day (mandatory)</li>
 * <li>business.end.hour - specifies ending hour of work day (mandatory)</li>
 * <li>business.holidays - specifies holidays (see format section for details on how to configure it)</li>
 * <li>business.holiday.date.format - specifies holiday date format used (default yyyy-MM-dd)</li>
 * <li>business.weekend.days - specifies days of the weekend (default Saturday (7) and Sunday (1), use 0 to indicate no weekend days)</li>
 * <li>business.cal.timezone - specifies time zone to be used (if not given uses default of the system it runs on)</li>
 * <li>business.hours.per.day - calculated as the difference between business.end.hour and business.start.hour</li>
 * <li>business.days.per.week - calculated as 7 - number of weekend days</li>
 * </ul>
 *
 * <b>Format</b><br/>
 *
 * Holidays can be given in two formats:
 * <ul>
 * <li>as date range separated with colon - for instance 2012-05-01:2012-05-15</li>
 * <li>single day holiday - for instance 2012-05-01</li>
 * </ul>
 * each holiday period should be separated from next one with comma: 2012-05-01:2012-05-15,2012-12-24:2012-12-27
 * <br/>
 * Holiday date format must be given in pattern that is supported by <code>java.text.SimpleDateFormat</code>.<br/>
 *
 * Weekend days should be given as integer that corresponds to <code>java.util.Calendar</code> constants, use 0 to indicate no weekend days
 * <br/>
 */
public class BusinessCalendarImpl implements BusinessCalendar {

    private static final Logger logger = LoggerFactory.getLogger(BusinessCalendarImpl.class);

    private static final long HOUR_IN_MILLIS = 60 * 60 * 1000;

    private final int daysPerWeek;
    private final int hoursInDay;
    private final int startHour;
    private final int endHour;
    private final String timezone;

    private final List<TimePeriod> holidays;
    private final List<Integer> weekendDays;

    private static final int SIM_WEEK = 3;
    private static final int SIM_DAY = 5;
    private static final int SIM_HOU = 7;
    private static final int SIM_MIN = 9;
    private static final int SIM_SEC = 11;

    public static final String START_HOUR = "business.start.hour";
    public static final String END_HOUR = "business.end.hour";
    // holidays are given as date range and can have more than one value separated with comma
    public static final String HOLIDAYS = "business.holidays";
    public static final String HOLIDAY_DATE_FORMAT = "business.holiday.date.format";

    public static final String WEEKEND_DAYS = "business.weekend.days";
    public static final String TIMEZONE = "business.cal.timezone";

    public BusinessCalendarImpl() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(BUSINESS_CALENDAR_PATH);
        if (Objects.nonNull(resource)) {
            Properties calendarConfiguration = new Properties();
            try (InputStream is = resource.openStream()) {
                calendarConfiguration.load(is);
                CalendarBean calendarBean = new CalendarBean(calendarConfiguration);
                holidays = calendarBean.getHolidays();
                weekendDays = calendarBean.getWeekendDays();
                daysPerWeek = calendarBean.getDaysPerWeek();
                timezone = calendarBean.getTimezone();
                startHour = calendarBean.getStartHour();
                endHour = calendarBean.getEndHour();
                hoursInDay = calendarBean.getHoursInDay();
            } catch (IOException e) {
                String errorMessage = "Error while loading properties for business calendar";
                logger.error(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            } catch (IllegalArgumentException e) {
                String errorMessage = "Error while populating properties for business calendar";
                logger.error(errorMessage, e);
                throw e;
            }
        } else {
            String errorMessage = String.format("Missing %s", BUSINESS_CALENDAR_PATH);
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    @Override
    public long calculateBusinessTimeAsDuration(String timeExpression) {
        timeExpression = adoptISOFormat(timeExpression);

        Date calculatedDate = calculateBusinessTimeAsDate(timeExpression);

        return (calculatedDate.getTime() - getCurrentTime());
    }

    @Override
    public Date calculateBusinessTimeAsDate(String timeExpression) {
        timeExpression = adoptISOFormat(timeExpression);

        String trimmed = timeExpression.trim();
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int min = 0;
        int sec = 0;

        if (trimmed.length() > 0) {
            Matcher mat = PatternConstants.SIMPLE_TIME_DATE_MATCHER.matcher(trimmed);
            if (mat.matches()) {
                weeks = (mat.group(SIM_WEEK) != null) ? Integer.parseInt(mat.group(SIM_WEEK)) : 0;
                days = (mat.group(SIM_DAY) != null) ? Integer.parseInt(mat.group(SIM_DAY)) : 0;
                hours = (mat.group(SIM_HOU) != null) ? Integer.parseInt(mat.group(SIM_HOU)) : 0;
                min = (mat.group(SIM_MIN) != null) ? Integer.parseInt(mat.group(SIM_MIN)) : 0;
                sec = (mat.group(SIM_SEC) != null) ? Integer.parseInt(mat.group(SIM_SEC)) : 0;
            }
        }
        int time = 0;

        Calendar c = new GregorianCalendar();
        if (timezone != null) {
            c.setTimeZone(TimeZone.getTimeZone(timezone));
        }

        // calculate number of weeks
        int numberOfWeeks = days / daysPerWeek + weeks;
        if (numberOfWeeks > 0) {
            c.add(Calendar.WEEK_OF_YEAR, numberOfWeeks);
        }
        handleWeekend(c, hours > 0 || min > 0);
        hours += (days - (numberOfWeeks * daysPerWeek)) * hoursInDay;

        // calculate number of days
        int numberOfDays = hours / hoursInDay;
        if (numberOfDays > 0) {
            for (int i = 0; i < numberOfDays; i++) {
                c.add(Calendar.DAY_OF_YEAR, 1);
                handleWeekend(c, false);
                handleHoliday(c, hours > 0 || min > 0);
            }
        }

        int currentCalHour = c.get(Calendar.HOUR_OF_DAY);
        if (currentCalHour >= endHour) {
            c.add(Calendar.DAY_OF_YEAR, 1);
            c.add(Calendar.HOUR_OF_DAY, startHour - currentCalHour);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
        } else if (currentCalHour < startHour) {
            c.add(Calendar.HOUR_OF_DAY, startHour);
        }

        // calculate remaining hours
        time = hours - (numberOfDays * hoursInDay);
        c.add(Calendar.HOUR, time);
        handleWeekend(c, true);
        handleHoliday(c, hours > 0 || min > 0);

        currentCalHour = c.get(Calendar.HOUR_OF_DAY);
        if (currentCalHour >= endHour) {
            c.add(Calendar.DAY_OF_YEAR, 1);
            // set hour to the starting one
            c.set(Calendar.HOUR_OF_DAY, startHour);
            c.add(Calendar.HOUR_OF_DAY, currentCalHour - endHour);
        } else if (currentCalHour < startHour) {
            c.add(Calendar.HOUR_OF_DAY, startHour);
        }

        // calculate minutes
        int numberOfHours = min / 60;
        if (numberOfHours > 0) {
            c.add(Calendar.HOUR, numberOfHours);
            min = min - (numberOfHours * 60);
        }
        c.add(Calendar.MINUTE, min);

        // calculate seconds
        int numberOfMinutes = sec / 60;
        if (numberOfMinutes > 0) {
            c.add(Calendar.MINUTE, numberOfMinutes);
            sec = sec - (numberOfMinutes * 60);
        }
        c.add(Calendar.SECOND, sec);

        currentCalHour = c.get(Calendar.HOUR_OF_DAY);
        if (currentCalHour >= endHour) {
            c.add(Calendar.DAY_OF_YEAR, 1);
            // set hour to the starting one
            c.set(Calendar.HOUR_OF_DAY, startHour);
            c.add(Calendar.HOUR_OF_DAY, currentCalHour - endHour);
        } else if (currentCalHour < startHour) {
            c.add(Calendar.HOUR_OF_DAY, startHour);
        }
        // take under consideration weekend
        handleWeekend(c, false);
        // take under consideration holidays
        handleHoliday(c, false);

        return c.getTime();
    }


    protected String adoptISOFormat(String timeExpression) {

        try {
            Duration p = null;
            if (DateTimeUtils.isPeriod(timeExpression)) {
                p = Duration.parse(timeExpression);
            } else if (DateTimeUtils.isNumeric(timeExpression)) {
                p = Duration.of(Long.valueOf(timeExpression), ChronoUnit.MILLIS);
            } else {
                OffsetDateTime dateTime = OffsetDateTime.parse(timeExpression, DateTimeFormatter.ISO_DATE_TIME);
                p = Duration.between(OffsetDateTime.now(), dateTime);
            }

            long days = p.toDays();
            long hours = p.toHours() % 24;
            long minutes = p.toMinutes() % 60;
            long seconds = p.getSeconds() % 60;
            long milis = p.toMillis() % 1000;

            StringBuffer time = new StringBuffer();
            if (days > 0) {
                time.append(days + "d");
            }
            if (hours > 0) {
                time.append(hours + "h");
            }
            if (minutes > 0) {
                time.append(minutes + "m");
            }
            if (seconds > 0) {
                time.append(seconds + "s");
            }
            if (milis > 0) {
                time.append(milis + "ms");
            }

            return time.toString();
        } catch (Exception e) {
            return timeExpression;
        }
    }

    protected void handleHoliday(Calendar c, boolean resetTime) {
        if (!holidays.isEmpty()) {
            Date current = c.getTime();
            for (TimePeriod holiday : holidays) {
                // check each holiday if it overlaps current date and break after first match
                if (current.after(holiday.getFrom()) && current.before(holiday.getTo())) {

                    Calendar tmp = new GregorianCalendar();
                    tmp.setTime(holiday.getTo());

                    Calendar tmp2 = new GregorianCalendar();
                    tmp2.setTime(current);
                    tmp2.set(Calendar.HOUR_OF_DAY, 0);
                    tmp2.set(Calendar.MINUTE, 0);
                    tmp2.set(Calendar.SECOND, 0);
                    tmp2.set(Calendar.MILLISECOND, 0);

                    long difference = tmp.getTimeInMillis() - tmp2.getTimeInMillis();

                    c.add(Calendar.HOUR_OF_DAY, (int) (difference / HOUR_IN_MILLIS));

                    handleWeekend(c, resetTime);
                    break;
                }
            }
        }

    }

    static class TimePeriod {
        private Date from;
        private Date to;

        protected TimePeriod(Date from, Date to) {
            this.from = from;
            this.to = to;
        }

        protected Date getFrom() {
            return this.from;
        }

        protected Date getTo() {
            return this.to;
        }
    }

    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }

    protected boolean isWorkingDay(int day) {
        return !weekendDays.contains(day);
    }

    protected void handleWeekend(Calendar c, boolean resetTime) {
        int dayOfTheWeek = c.get(Calendar.DAY_OF_WEEK);
        while (!isWorkingDay(dayOfTheWeek)) {
            c.add(Calendar.DAY_OF_YEAR, 1);
            if (resetTime) {
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
            }
            dayOfTheWeek = c.get(Calendar.DAY_OF_WEEK);
        }
    }


}
