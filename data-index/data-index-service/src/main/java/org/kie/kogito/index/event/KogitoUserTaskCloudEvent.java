/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.index.event;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.kie.kogito.index.model.UserTaskInstance;

public class KogitoUserTaskCloudEvent extends KogitoCloudEvent<UserTaskInstance> {

    @JsonProperty("kogitoUserTaskinstanceId")
    private String userTaskInstanceId;

    @JsonProperty("kogitoUserTaskinstanceState")
    private String state;

    public static Builder builder() {
        return new Builder();
    }

    public String getUserTaskInstanceId() {
        return userTaskInstanceId;
    }

    public void setUserTaskInstanceId(String userTaskInstanceId) {
        this.userTaskInstanceId = userTaskInstanceId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public void setTime(ZonedDateTime time) {
        super.setTime(time);
        if (getData() != null && time != null) {
            getData().setLastUpdate(time);
        }
    }

    @Override
    public String toString() {
        return "KogitoUserTaskCloudEvent{" +
                "userTaskInstanceId='" + userTaskInstanceId + '\'' +
                ", state='" + state + '\'' +
                "} " + super.toString();
    }

    public static final class Builder extends AbstractBuilder<Builder, UserTaskInstance, KogitoUserTaskCloudEvent> {

        private Builder() {
            super(new KogitoUserTaskCloudEvent());
        }

        public Builder userTaskInstanceId(String userTaskInstanceId) {
            event.setUserTaskInstanceId(userTaskInstanceId);
            return this;
        }

        public Builder state(String state) {
            event.setState(state);
            return this;
        }
    }
}
