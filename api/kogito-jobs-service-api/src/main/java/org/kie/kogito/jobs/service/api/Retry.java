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

package org.kie.kogito.jobs.service.api;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The retry configuration establishes the number of times a failing Job execution must be retried before it’s considered as FAILED")
public class Retry {

    private int maxRetries;
    private long delay;
    private TemporalUnit delayUnit;
    private long maxDuration;
    private TemporalUnit durationUnit;

    public Retry() {
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
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

    public long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public TemporalUnit getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(TemporalUnit durationUnit) {
        this.durationUnit = durationUnit;
    }

    @Override
    public String toString() {
        return "Retry{" +
                "maxRetries=" + maxRetries +
                ", delay=" + delay +
                ", delayUnit='" + delayUnit + '\'' +
                ", maxDuration=" + maxDuration +
                ", durationUnit='" + durationUnit + '\'' +
                '}';
    }
}
