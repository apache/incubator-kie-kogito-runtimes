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
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.drools.base.time.TimeUtils;

public class DateTimeUtils extends TimeUtils {

    public static boolean isRepeatable(String dateTimeStr) {
        return dateTimeStr != null && dateTimeStr.startsWith("R");
    }

    public static boolean isPeriod(String dateTimeStr) {
        return dateTimeStr != null && dateTimeStr.startsWith("P");
    }

    public static boolean isNumeric(String dateTimeStr) {
        if (dateTimeStr != null) {
            return dateTimeStr.chars().allMatch(Character::isDigit);
        }

        return false;
    }

    public static long parseDateTime(String dateTimeStr) {
        OffsetDateTime dateTime = OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        return dateTime.toInstant().toEpochMilli();
    }

    public static long parseDuration(String durationStr) {
        if (isPeriod(durationStr)) {
            return Duration.parse(durationStr).toMillis();
        } else {
            return TimeUtils.parseTimeString(durationStr);
        }
    }

    public static long parseDateAsDuration(String dateTimeStr) {
        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
            Duration duration = Duration.between(OffsetDateTime.now(), dateTime);

            return duration.toMillis();
        } catch (Exception e) {
            return TimeUtils.parseTimeString(dateTimeStr);
        }
    }

    public static String[] parseISORepeatable(String isoString) {
        String[] result = new String[3];
        String[] elements = isoString.split("/");
        if (elements.length == 3) {
            result[0] = elements[0].substring(1);
            result[1] = elements[1];
            result[2] = elements[2];
        } else {
            result[0] = elements[0].substring(1);
            result[1] = OffsetDateTime.now().plus(Duration.of(getMillis(elements[1]), ChronoUnit.MILLIS)).format(DateTimeFormatter.ISO_DATE_TIME);
            result[2] = elements[1];
        }

        return result;
    }

    public static long getMillis(String durationStr) {
        if (durationStr.startsWith("PT")) { // ISO-8601 PTnHnMn.nS
            return Duration.parse(durationStr).toMillis();
        } else if (!durationStr.contains("T")) { // ISO-8601 PnYnMnWnD
            Period period = Period.parse(durationStr);
            OffsetDateTime now = OffsetDateTime.now();
            return Duration.between(now, now.plus(period)).toMillis();
        } else { // ISO-8601 PnYnMnWnDTnHnMn.nS
            String[] elements = durationStr.split("T");
            Period period = Period.parse(elements[0]);
            Duration duration = Duration.parse("PT" + elements[1]);
            OffsetDateTime now = OffsetDateTime.now();

            return Duration.between(now, now.plus(period).plus(duration)).toMillis();
        }
    }

    public static long[] parseRepeatableDateTime(String dateTimeStr) {
        long[] result = new long[3];
        if (isRepeatable(Objects.requireNonNull(dateTimeStr, "Date-time string cannot be a null value!"))) {

            String[] parsed = parseISORepeatable(dateTimeStr);
            String repeats = parsed[0];
            String delayIn = parsed[1];
            String periodIn = parsed[2];

            Duration startAtDelayDur = null;
            Duration period = null;

            if (DateTimeUtils.isPeriod(delayIn)) {
                // If delay is specified as duration then period variable carry end time information
                OffsetDateTime endTime = OffsetDateTime.parse(periodIn, DateTimeFormatter.ISO_DATE_TIME);
                period = Duration.parse(delayIn);
                startAtDelayDur = Duration.between(OffsetDateTime.now(), endTime.minus(period));
            } else if (DateTimeUtils.isPeriod(periodIn)) {
                // If period is specified as duration then delay variable carry start time information
                OffsetDateTime startTime = OffsetDateTime.parse(delayIn, DateTimeFormatter.ISO_DATE_TIME);
                period = Duration.of(getMillis(periodIn), ChronoUnit.MILLIS);
                startAtDelayDur = Duration.between(OffsetDateTime.now(), startTime);
            } else {
                // Both delay and period are specified as start and end times
                OffsetDateTime startTime = OffsetDateTime.parse(delayIn, DateTimeFormatter.ISO_DATE_TIME);
                OffsetDateTime endTime = OffsetDateTime.parse(periodIn, DateTimeFormatter.ISO_DATE_TIME);
                startAtDelayDur = Duration.between(OffsetDateTime.now(), startTime);
                period = Duration.between(startTime, endTime);
            }

            if (startAtDelayDur.isNegative() || startAtDelayDur.isZero()) {
                // need to introduce delay to allow all initialization
                startAtDelayDur = Duration.of(1, ChronoUnit.SECONDS);
            }

            result[0] = Long.parseLong(repeats.length() == 0 ? "-1" : repeats);
            result[1] = startAtDelayDur.toMillis();
            result[2] = period.toMillis();

            return result;
        } else {

            int index = dateTimeStr.indexOf("###");
            if (index != -1) {
                String period = dateTimeStr.substring(index + 3);
                String delay = dateTimeStr.substring(0, index);
                result = new long[] { TimeUtils.parseTimeString(delay), TimeUtils.parseTimeString(period) };

                return result;
            }
            result = new long[] { TimeUtils.parseTimeString(dateTimeStr) };
            return result;
        }
    }
}
