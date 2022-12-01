/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.jobs.service.api.schedule.timer;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.kie.kogito.jobs.service.api.Schedule;
import org.kie.kogito.jobs.service.api.TemporalUnit;

@Schema(description = "Schedule definition to execute a job on a timer based configuration", allOf = { Schedule.class })
public class TimerSchedule extends Schedule {

    private String startTime;
    private int repeatCount = 0;
    private long delay = 0;
    private TemporalUnit delayUnit = TemporalUnit.MILLIS;

    public TimerSchedule() {
        // marshalling constructor.
    }

    public TimerSchedule(String startTime, int repeatCount, long delay, TemporalUnit delayUnit) {
        this.startTime = startTime;
        this.repeatCount = repeatCount;
        this.delay = delay;
        this.delayUnit = delayUnit;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public TemporalUnit getDelayUnit() {
        return delayUnit;
    }

    public void setDelayUnit(TemporalUnit delayUnit) {
        this.delayUnit = delayUnit;
    }

    @Override
    public String toString() {
        return "TimerSchedule{" +
                "startTime='" + startTime + '\'' +
                ", repeatCount=" + repeatCount +
                ", delay=" + delay +
                ", delayUnit='" + delayUnit + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder(new TimerSchedule());
    }

    public static class Builder {

        private final TimerSchedule schedule;

        private Builder(TimerSchedule schedule) {
            this.schedule = schedule;
        }

        public Builder startTime(String startTime) {
            schedule.setStartTime(startTime);
            return this;
        }

        public Builder repeatCount(int repeatCount) {
            schedule.setRepeatCount(repeatCount);
            return this;
        }

        public Builder delay(long delay) {
            schedule.setDelay(delay);
            return this;
        }

        public Builder delayUnit(TemporalUnit delayUnit) {
            schedule.setDelayUnit(delayUnit);
            return this;
        }

        public TimerSchedule build() {
            return schedule;
        }
    }
}
