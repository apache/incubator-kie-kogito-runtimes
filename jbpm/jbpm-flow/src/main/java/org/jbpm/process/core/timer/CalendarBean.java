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

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.process.core.timer.BusinessCalendarImpl.END_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAYS;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.HOLIDAY_DATE_FORMAT;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.START_HOUR;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.TIMEZONE;
import static org.jbpm.process.core.timer.BusinessCalendarImpl.WEEKEND_DAYS;

public class CalendarBean {

    private static final Logger logger = LoggerFactory.getLogger(CalendarBean.class);


    private final Properties calendarConfiguration;
    private static final Collection<String> REQUIRED_PROPERTIES = Arrays.asList(START_HOUR, END_HOUR, WEEKEND_DAYS);

    private static final Map<String, BiConsumer<StringBuilder, Properties>> FORMAL_VALIDATOR_MAP;
    private static final List<BiConsumer<StringBuilder, Properties>> BUSINESS_VALIDATOR_LIST;

    private static final int LOWER_HOUR_BOUND = 0;

    private static final int UPPER_HOUR_BOUND = 24;

    private static final String DEFAULT_WEEKENDS = String.format("%s,%s", Calendar.SATURDAY, Calendar.SUNDAY);
    private static final String DEFAULT_HOLIDAY_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_TIMEZONE = TimeZone.getDefault().getID();

    static {
        FORMAL_VALIDATOR_MAP = new HashMap<>();
        FORMAL_VALIDATOR_MAP.put(START_HOUR, (stringBuilder, properties) -> {
            if (properties.containsKey(START_HOUR)) {
                try {
                    int hour = getPropertyAsInt(START_HOUR, properties);
                    if (!isInsideValidRange(hour, LOWER_HOUR_BOUND, UPPER_HOUR_BOUND)) {
                        addMessageToStringBuilder(stringBuilder, String.format("Start hour %s outside expected boundaries (0-24)", hour));
                    }
                } catch (NumberFormatException e) {
                    addMessageToStringBuilder(stringBuilder, e.getMessage());
                }
            }
        });
        FORMAL_VALIDATOR_MAP.put(END_HOUR, (stringBuilder, properties) -> {
            if (properties.containsKey(END_HOUR)) {
                try {
                    int hour = getPropertyAsInt(END_HOUR, properties);
                    if (!isInsideValidRange(hour, LOWER_HOUR_BOUND, UPPER_HOUR_BOUND)) {
                        addMessageToStringBuilder(stringBuilder, String.format("Start hour %s outside expected boundaries (0-24)", hour));
                    }
                } catch (NumberFormatException e) {
                    addMessageToStringBuilder(stringBuilder, e.getMessage());
                }
            }
        });
        FORMAL_VALIDATOR_MAP.put(HOLIDAYS, (stringBuilder, properties) -> {
            if (properties.containsKey(HOLIDAYS)) {
                String originalData = properties.getProperty(WEEKEND_DAYS);
                String[] allHolidays = originalData.split(",");
                for (String holiday : allHolidays) {
                    String[] ranges = holiday.split(":");
                    for (String range : ranges) {
                        try {
                            getFormattedDate(range, properties);
                        } catch (ParseException e) {
                            addMessageToStringBuilder(stringBuilder, e.getMessage());
                        }
                    }
                }
            }
        });
        FORMAL_VALIDATOR_MAP.put(HOLIDAY_DATE_FORMAT, (stringBuilder, properties) -> {
            if (properties.containsKey(HOLIDAY_DATE_FORMAT)) {
                try {
                    getSimpleDateFormat((String) properties.get(HOLIDAY_DATE_FORMAT));
                } catch (IllegalArgumentException e) {
                    addMessageToStringBuilder(stringBuilder, e.getMessage());
                }
            }
        });
        FORMAL_VALIDATOR_MAP.put(WEEKEND_DAYS, (stringBuilder, properties) -> {
            if (properties.containsKey(WEEKEND_DAYS)) {
                String originalData = properties.getProperty(WEEKEND_DAYS);
                String[] weekendDays = originalData.split(",");
                Set<String> differentValues = Arrays.stream(weekendDays).collect(Collectors.toSet());
                if (differentValues.size() < weekendDays.length) {
                    addMessageToStringBuilder(stringBuilder, String.format("There are repeated values in the given WEEKEND_DAYS %s", originalData));
                }
                if (differentValues.contains("0") && differentValues.size() > 1) {
                    addMessageToStringBuilder(stringBuilder, String.format("0 (= no weekends) and other values provided in the given WEEKEND_DAYS %s", originalData));
                }
                if (differentValues.size() > 7) {
                    addMessageToStringBuilder(stringBuilder, String.format("It is not possible to have more then 7 WEEKEND_DAYS; provided %s", differentValues.size()));
                }
                differentValues.forEach(s -> {
                    try {
                        getStringAsInt(s);
                    } catch (NumberFormatException e) {
                        addMessageToStringBuilder(stringBuilder, e.getMessage());
                    }
                });
            }
        });
        FORMAL_VALIDATOR_MAP.put(TIMEZONE, (stringBuilder, properties) -> {
            if (properties.containsKey(TIMEZONE)) {
                String originalData = properties.getProperty(WEEKEND_DAYS);
                try {
                    TimeZone.getTimeZone(originalData);
                } catch (Throwable e) {
                    addMessageToStringBuilder(stringBuilder, e.getMessage());
                }
            }
        });
        BUSINESS_VALIDATOR_LIST = new ArrayList<>();
        BUSINESS_VALIDATOR_LIST.add((stringBuilder, properties) -> {
            int startHour = getPropertyAsInt(START_HOUR, properties);
            int endHour = getPropertyAsInt(END_HOUR, properties);
            if (startHour == endHour) {
                addMessageToStringBuilder(stringBuilder, String.format("Start hour %s and end hour %s must be different", startHour, endHour));
            }
        });
    }

    private CalendarBean(Properties calendarConfiguration) {
        this.calendarConfiguration = calendarConfiguration;
        setup();
    }

    public static CalendarBean create(Properties calendarConfiguration) {
        return new CalendarBean(calendarConfiguration);
    }

    static void formalValidation(StringBuilder errorMessage, Properties calendarConfiguration) {
        requiredValidation(errorMessage, calendarConfiguration);
        formatValidation(errorMessage, calendarConfiguration);
    }

    static void requiredValidation(StringBuilder errorMessage, Properties calendarConfiguration) {
        REQUIRED_PROPERTIES.forEach(property -> validateRequiredProperty(property, errorMessage, calendarConfiguration));
    }

    static void formatValidation(StringBuilder errorMessage, Properties calendarConfiguration) {
        FORMAL_VALIDATOR_MAP.values().forEach(stringBuilderPropertiesBiConsumer -> stringBuilderPropertiesBiConsumer.accept(errorMessage, calendarConfiguration));
    }

    static void missingDataPopulation(Properties calendarConfiguration) {
        if (!calendarConfiguration.containsKey(WEEKEND_DAYS)) {
            calendarConfiguration.put(WEEKEND_DAYS, DEFAULT_WEEKENDS);
        }
        if (!calendarConfiguration.containsKey(HOLIDAY_DATE_FORMAT)) {
            calendarConfiguration.put(HOLIDAY_DATE_FORMAT, DEFAULT_HOLIDAY_DATE_FORMAT);
        }
        if (!calendarConfiguration.containsKey(TIMEZONE)) {
            calendarConfiguration.put(TIMEZONE, DEFAULT_TIMEZONE);
        }
    }

    static void businessValidation(StringBuilder errorMessage, Properties calendarConfiguration) {
        BUSINESS_VALIDATOR_LIST.forEach(stringBuilderPropertiesBiConsumer -> stringBuilderPropertiesBiConsumer.accept(errorMessage, calendarConfiguration));
    }

    static int getPropertyAsInt(String propertyName, Properties calendarConfiguration) {
        String value = calendarConfiguration.getProperty(propertyName);
        return getStringAsInt(value);
    }

    static int getStringAsInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            logger.error("Number format exception while parsing {} {}", value, nfe.getMessage());
            throw nfe;
        }
    }

    static Date getFormattedDate(String date, Properties businessCalendar) throws ParseException {
        SimpleDateFormat sdf =
                businessCalendar.contains(HOLIDAY_DATE_FORMAT) ? getSimpleDateFormat(businessCalendar.getProperty(HOLIDAY_DATE_FORMAT)) : getSimpleDateFormat(DEFAULT_HOLIDAY_DATE_FORMAT);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (date.startsWith("*")) {
            date = date.replaceFirst("\\*", currentYear + "");
        }
        return sdf.parse(date);
    }

    static SimpleDateFormat getSimpleDateFormat(String format) throws IllegalArgumentException {
        return new SimpleDateFormat(format);
    }

    static void validateRequiredProperty(String property, StringBuilder errorMessage, Properties calendarConfiguration) {
        String value = calendarConfiguration.getProperty(property);
        if (Objects.isNull(value)) {
            errorMessage.append("Property required: ").append(property).append(" is not provided.\n");
        }
    }

    static boolean isInsideValidRange(int value, int lowerBound, int upperBound) {
        return value >= lowerBound && value <= upperBound;
    }

    private static void addMessageToStringBuilder(StringBuilder stringBuilder, String message) {
        stringBuilder.append(message);
        stringBuilder.append("\n");
    }


    public List<BusinessCalendarImpl.TimePeriod> getHolidays() {
        if (!calendarConfiguration.contains(HOLIDAYS)) {
            return Collections.emptyList();
        }
        String timezone = calendarConfiguration.getProperty(TIMEZONE);

        String holidaysString = calendarConfiguration.getProperty(HOLIDAYS);
        List<BusinessCalendarImpl.TimePeriod> holidays = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] hPeriods = holidaysString.split(",");

        for (String hPeriod : hPeriods) {
            boolean addNextYearHolidays = false;

            String[] fromTo = hPeriod.split(":");
            if (fromTo[0].startsWith("*")) {
                addNextYearHolidays = true;

                fromTo[0] = fromTo[0].replaceFirst("\\*", currentYear + "");
            }
            try {
                if (fromTo.length == 2) {
                    Calendar tmpFrom = new GregorianCalendar();
                    if (timezone != null) {
                        tmpFrom.setTimeZone(TimeZone.getTimeZone(timezone));
                    }
                    tmpFrom.setTime(getFormattedDate(fromTo[0], calendarConfiguration));

                    if (fromTo[1].startsWith("*")) {

                        fromTo[1] = fromTo[1].replaceFirst("\\*", currentYear + "");
                    }

                    Calendar tmpTo = new GregorianCalendar();
                    if (timezone != null) {
                        tmpTo.setTimeZone(TimeZone.getTimeZone(timezone));
                    }
                    tmpTo.setTime(getFormattedDate(fromTo[1], calendarConfiguration));
                    Date from = tmpFrom.getTime();

                    tmpTo.add(Calendar.DAY_OF_YEAR, 1);

                    if ((tmpFrom.get(Calendar.MONTH) > tmpTo.get(Calendar.MONTH)) && (tmpFrom.get(Calendar.YEAR) == tmpTo.get(Calendar.YEAR))) {
                        tmpTo.add(Calendar.YEAR, 1);
                    }

                    Date to = tmpTo.getTime();
                    holidays.add(new BusinessCalendarImpl.TimePeriod(from, to));

                    holidays.add(new BusinessCalendarImpl.TimePeriod(from, to));
                    if (addNextYearHolidays) {
                        tmpFrom = new GregorianCalendar();
                        if (timezone != null) {
                            tmpFrom.setTimeZone(TimeZone.getTimeZone(timezone));
                        }
                        tmpFrom.setTime(getFormattedDate(fromTo[0], calendarConfiguration));
                        tmpFrom.add(Calendar.YEAR, 1);

                        from = tmpFrom.getTime();
                        tmpTo = new GregorianCalendar();
                        if (timezone != null) {
                            tmpTo.setTimeZone(TimeZone.getTimeZone(timezone));
                        }
                        tmpTo.setTime(getFormattedDate(fromTo[1], calendarConfiguration));
                        tmpTo.add(Calendar.YEAR, 1);
                        tmpTo.add(Calendar.DAY_OF_YEAR, 1);

                        if ((tmpFrom.get(Calendar.MONTH) > tmpTo.get(Calendar.MONTH)) && (tmpFrom.get(Calendar.YEAR) == tmpTo.get(Calendar.YEAR))) {
                            tmpTo.add(Calendar.YEAR, 1);
                        }

                        to = tmpTo.getTime();
                        holidays.add(new BusinessCalendarImpl.TimePeriod(from, to));
                    }
                } else {

                    Calendar c = new GregorianCalendar();
                    c.setTime(getFormattedDate(fromTo[0], calendarConfiguration));
                    c.add(Calendar.DAY_OF_YEAR, 1);
                    // handle one day holiday
                    holidays.add(new BusinessCalendarImpl.TimePeriod(getFormattedDate(fromTo[0], calendarConfiguration), c.getTime()));
                    if (addNextYearHolidays) {
                        Calendar tmp = Calendar.getInstance();
                        tmp.setTime(getFormattedDate(fromTo[0], calendarConfiguration));
                        tmp.add(Calendar.YEAR, 1);

                        Date from = tmp.getTime();
                        c.add(Calendar.YEAR, 1);
                        holidays.add(new BusinessCalendarImpl.TimePeriod(from, c.getTime()));
                    }
                }
            } catch (Exception e) {
                logger.error("Error while parsing holiday in business calendar", e);
            }
        }
        return holidays;
    }

    public List<Integer> getWeekendDays() {
        return parseWeekendDays(calendarConfiguration);
    }

    public int getDaysPerWeek() {
        return 7 - parseWeekendDays(calendarConfiguration).size();
    }

    public String getTimezone() {
        return calendarConfiguration.getProperty(TIMEZONE);
    }

    public int getStartHour() {
        return getPropertyAsInt(START_HOUR);
    }

    public int getEndHour() {
        return getPropertyAsInt(END_HOUR);
    }

    public int getHoursInDay() {
        int startHour = getStartHour();
        int endHour = getEndHour();
        return startHour < endHour ? endHour - startHour : (24 - startHour) + endHour;
    }

    protected void setup() {
        StringBuilder errorMessage = new StringBuilder();
        formalValidation(errorMessage, calendarConfiguration);
        missingDataPopulation(calendarConfiguration);
        businessValidation(errorMessage, calendarConfiguration);
        if (!errorMessage.isEmpty()) {
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }

    protected List<Integer> parseWeekendDays(Properties calendarConfiguration) {
        String weekendDays = calendarConfiguration.getProperty(WEEKEND_DAYS);
        String[] days = weekendDays.split(",");
        return Arrays.stream(days).map(day -> Integer.parseInt(day.trim()))
                .filter(intDay -> intDay != 0)
                .collect(Collectors.toList());
    }

    protected int getPropertyAsInt(String propertyName) {
        return getPropertyAsInt(propertyName, calendarConfiguration);
    }
}