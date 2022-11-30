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

package org.kie.kogito.jobs.service.api.schedule.cron;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.kie.kogito.jobs.service.api.Schedule;

@Schema(description = "Schedule definition to execute a job on a cron based configuration", allOf = { Schedule.class })
public class CronSchedule extends Schedule {

    private String expression;
    private String timeZone;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public CronSchedule() {
    }

    public CronSchedule(String expression, String timeZone) {
        this.expression = expression;
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return "CronSchedule{" +
                "expression='" + expression + '\'' +
                ", timeZone='" + timeZone + '\'' +
                '}';
    }
}
