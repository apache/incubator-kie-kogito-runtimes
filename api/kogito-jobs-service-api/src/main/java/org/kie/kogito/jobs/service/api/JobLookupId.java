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

public class JobLookupId {

    private String id;
    private String businessKey;

    private JobLookupId() {
        // marshalling constructor.
    }

    private JobLookupId(String id, String businessKey) {
        this.id = id;
        this.businessKey = businessKey;
    }

    public String getId() {
        return id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public static JobLookupId fromId(String id) {
        return new JobLookupId(id, null);
    }

    public static JobLookupId fromBusinessKey(String businessKey) {
        return new JobLookupId(null, businessKey);
    }
}
