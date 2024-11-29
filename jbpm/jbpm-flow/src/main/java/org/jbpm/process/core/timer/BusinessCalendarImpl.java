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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;

import org.jbpm.util.PatternConstants;
import org.kie.kogito.calendar.BusinessCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static Builder builder() {
        return new Builder();
    }

    private BusinessCalendarImpl() {
        this(CalendarBeanFactory.createCalendarBean());
    }

    private BusinessCalendarImpl(CalendarBean calendarBean) {
        holidays = calendarBean.getHolidays();
        weekendDays = calendarBean.getWeekendDays();
        daysPerWeek = calendarBean.getDaysPerWeek();
        timezone = calendarBean.getTimezone();
        startHour = calendarBean.getStartHour();
        endHour = calendarBean.getEndHour();
        hoursInDay = calendarBean.getHoursInDay();
    }

    /**
     * @inheritDoc
     */
    @Override
    public long calculateBusinessTimeAsDuration(String timeExpression) {
        timeExpression = adoptISOFormat(timeExpression);

        Date calculatedDate = calculateBusinessTimeAsDate(timeExpression);

        return (calculatedDate.getTime() - getCurrentTime());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Date calculateBusinessTimeAsDate(String timeExpression) {
        timeExpression = adoptISOFormat(timeExpression);

        String trimmed = timeExpression.trim();
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int min = 0;
        int sec = 0;

        if (!trimmed.isEmpty()) {
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

        Calendar calendar = new GregorianCalendar();
        if (timezone != null) {
            calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        }

        // calculate number of weeks
        int numberOfWeeks = days / daysPerWeek + weeks;
        if (numberOfWeeks > 0) {
            calendar.add(Calendar.WEEK_OF_YEAR, numberOfWeeks);
        }
        rollCalendarToNextWorkingDay(calendar, hours > 0 || min > 0);
        hours += (days - (numberOfWeeks * daysPerWeek)) * hoursInDay;

        // calculate number of days
        int numberOfDays = hours / hoursInDay;
        if (numberOfDays > 0) {
            for (int i = 0; i < numberOfDays; i++) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                rollCalendarToNextWorkingDay(calendar, false);
                rollCalendarAfterHolidays(calendar, hours > 0 || min > 0);
            }
        }

        int currentCalHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (currentCalHour >= endHour) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.add(Calendar.HOUR_OF_DAY, startHour - currentCalHour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else if (currentCalHour < startHour) {
            calendar.add(Calendar.HOUR_OF_DAY, startHour - currentCalHour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }

        // calculate remaining hours
        time = hours - (numberOfDays * hoursInDay);
        calendar.add(Calendar.HOUR, time);
        rollCalendarToNextWorkingDay(calendar, true);
        rollCalendarAfterHolidays(calendar, hours > 0 || min > 0);

        currentCalHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (currentCalHour >= endHour) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            // set hour to the starting one
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.add(Calendar.HOUR_OF_DAY, currentCalHour - endHour);
        } else if (currentCalHour < startHour) {
            calendar.add(Calendar.HOUR_OF_DAY, startHour - currentCalHour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }

        // calculate minutes
        int numberOfHours = min / 60;
        if (numberOfHours > 0) {
            calendar.add(Calendar.HOUR, numberOfHours);
            min = min - (numberOfHours * 60);
        }
        calendar.add(Calendar.MINUTE, min);

        // calculate seconds
        int numberOfMinutes = sec / 60;
        if (numberOfMinutes > 0) {
            calendar.add(Calendar.MINUTE, numberOfMinutes);
            sec = sec - (numberOfMinutes * 60);
        }
        calendar.add(Calendar.SECOND, sec);

        currentCalHour = calendar.get(Calendar.HOUR_OF_DAY);
        // TODO - implement switching logic for night -hours
        if (currentCalHour >= endHour) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            // set hour to the starting one
            calendar.set(Calendar.HOUR_OF_DAY, startHour);
            calendar.add(Calendar.HOUR_OF_DAY, currentCalHour - endHour);
        } else if (currentCalHour < startHour) {
            calendar.add(Calendar.HOUR_OF_DAY, startHour - currentCalHour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }
        // take under consideration weekend
        rollCalendarToNextWorkingDay(calendar, false);
        // take under consideration holidays
        rollCalendarAfterHolidays(calendar, false);

        return calendar.getTime();
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

    /**
     * Rolls the given <code>Calendar</code> to the first <b>working day</b>
     * after configured <code>holidays</code>, if provided.
     *
     * Set hour, minute, second and millisecond when
     * <code>resetTime</code> is <code>true</code>
     * @param toRoll
     * @param resetTime
     */
    protected void rollCalendarAfterHolidays(Calendar toRoll, boolean resetTime) {
        if (!holidays.isEmpty()) {
            Date current = toRoll.getTime();
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

                    toRoll.add(Calendar.HOUR_OF_DAY, (int) (difference / HOUR_IN_MILLIS));

                    rollCalendarToNextWorkingDay(toRoll, resetTime);
                    break;
                }
            }
        }

    }

    /**
     * Rolls the given <code>Calendar</code> to the first <b>working day</b>
     * Set hour, minute, second and millisecond when
     * <code>resetTime</code> is <code>true</code>
     * @param toRoll
     * @param resetTime
     */
    protected void rollCalendarToNextWorkingDay(Calendar toRoll, boolean resetTime) {
        int dayOfTheWeek = toRoll.get(Calendar.DAY_OF_WEEK);
        while (!isWorkingDay(dayOfTheWeek)) {
            toRoll.add(Calendar.DAY_OF_YEAR, 1);
            if (resetTime) {
                toRoll.set(Calendar.HOUR_OF_DAY, 0);
                toRoll.set(Calendar.MINUTE, 0);
                toRoll.set(Calendar.SECOND, 0);
                toRoll.set(Calendar.MILLISECOND, 0);
            }
            dayOfTheWeek = toRoll.get(Calendar.DAY_OF_WEEK);
        }
    }

    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }

    protected boolean isWorkingDay(int day) {
        return !weekendDays.contains(day);
    }

    public static class Builder {

        private CalendarBean calendarBean;

        public Builder withCalendarBean(CalendarBean calendarBean) {
            this.calendarBean = calendarBean;
            return this;
        }

        public BusinessCalendarImpl build() {
            return calendarBean == null ? new BusinessCalendarImpl() : new BusinessCalendarImpl(calendarBean);
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TimePeriod that)) {
                return false;
            }
            return Objects.equals(from, that.from) && Objects.equals(to, that.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }

}
