/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.timer.impl;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.kie.kogito.timer.Trigger;

public class SimpleTimerTrigger implements Trigger {

    private static Set<ChronoUnit> acceptedUnits() {
        Set<ChronoUnit> units = new TreeSet<>(Enum::compareTo);
        units.add(ChronoUnit.MILLIS);
        units.add(ChronoUnit.SECONDS);
        units.add(ChronoUnit.MINUTES);
        units.add(ChronoUnit.HOURS);
        units.add(ChronoUnit.DAYS);
        return units;
    }

    private static final Set<ChronoUnit> ACCEPTED_CHRONO_UNITS = acceptedUnits();

    private Date startTime;

    private long period;

    private ChronoUnit periodUnit;

    private long repeatLimit;

    private Date endTime;

    private long repeatCount;

    private transient long periodInMillis;

    private Date nextFireTime;

    private String timeOffsetId;

    /**
     * @param startTime The trigger start time.
     * @param period The period for the calculation of the subsequent executions.
     * @param periodUnit The time unit in which the period is expressed. For, example, a period 2 ChronoUnit.SECONDS.
     *        When the periodUnit is null, ChronoUnit.MILLIS are assumed.
     * @param repeatLimit Number of times the trigger should be repeated according to the configured period.
     *        When the repeatLimit == 0, the timer is executed only one time at the startTime.
     * @param endTime An optional value indicating a potential endTime. Independently of the configured repeatLimit,
     *        a nextFireTime must never be after the endTime.
     * @param timeOffsetId An optional value indicating an ISO 8601 date-time shift from UTC/Greenwich, e.g. '+04:00'.
     *        While al the trigger and results and calculations are represented with the instant based
     *        java.util.Date, this value can be helpful in situations where the creator of the trigger
     *        wants to register the startTime's and endTime's original local-date time shift in order to
     *        translate the fireTimes calculate by the trigger to that zone.
     */
    public SimpleTimerTrigger(Date startTime, long period, ChronoUnit periodUnit,
            long repeatLimit, Date endTime, String timeOffsetId) {
        Objects.requireNonNull(startTime, "startTime have a non null value.");
        this.startTime = startTime;
        this.period = period;
        this.periodUnit = periodUnit;
        this.repeatLimit = repeatLimit;
        this.endTime = endTime;
        this.timeOffsetId = timeOffsetId;

        this.repeatCount = 0;
        this.nextFireTime = startTime;
        setPeriodUnit(periodUnit);
        setPeriodInMillis();
    }

    private void setPeriodInMillis() {
        if (periodUnit != null) {
            this.periodInMillis = periodUnit.getDuration().multipliedBy(period).toMillis();
        } else {
            periodInMillis = period;
        }
    }

    @Override
    public Date hasNextFireTime() {
        return nextFireTime;
    }

    @Override
    public synchronized Date nextFireTime() {
        if (nextFireTime == null) {
            return null;
        }
        final Date current = nextFireTime;
        repeatCount++;
        if (repeatCount > repeatLimit) {
            this.nextFireTime = null;
        } else {
            this.nextFireTime = new Date(current.getTime() + periodInMillis);
            if (endTime != null && nextFireTime.after(endTime)) {
                this.nextFireTime = null;
            }
        }
        return current;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
        setPeriodInMillis();
    }

    public ChronoUnit getPeriodUnit() {
        return periodUnit;
    }

    public void setPeriodUnit(ChronoUnit periodUnit) {
        if (periodUnit != null && !ACCEPTED_CHRONO_UNITS.contains(periodUnit)) {
            throw new IllegalArgumentException("The periodUnit must be one of the following values: " +
                    ACCEPTED_CHRONO_UNITS + ", but is: " + periodUnit);
        }
        this.periodUnit = periodUnit;
        setPeriodInMillis();
    }

    public long getRepeatLimit() {
        return repeatLimit;
    }

    public void setRepeatLimit(long repeatLimit) {
        this.repeatLimit = repeatLimit;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(long repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public String getTimeOffsetId() {
        return timeOffsetId;
    }

    public void setTimeOffsetId(String timeOffsetId) {
        this.timeOffsetId = timeOffsetId;
    }
}