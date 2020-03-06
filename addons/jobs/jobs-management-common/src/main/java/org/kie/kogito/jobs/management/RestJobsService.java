/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.jobs.management;

import java.net.URI;
import java.util.Objects;

import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.jobs.ProcessInstanceJobDescription;
import org.kie.kogito.jobs.api.URIBuilder;

public abstract class RestJobsService implements JobsService {

    public static final String JOBS_PATH = "/jobs";

    private URI jobsServiceUri;
    private String callbackEndpoint;

    public RestJobsService(String jobServiceUrl, String callbackEndpoint) {
        this.jobsServiceUri = Objects.nonNull(jobServiceUrl) ? buildJobsServiceURI(jobServiceUrl) : null;
        this.callbackEndpoint = callbackEndpoint;
    }

    public String getCallbackEndpoint(ProcessInstanceJobDescription description) {
        return URIBuilder.toURI(callbackEndpoint
                                        + "/management/jobs/"
                                        + description.processId()
                                        + "/instances/"
                                        + description.processInstanceId()
                                        + "/timers/"
                                        + description.id())
                .toString();
    }

    private URI buildJobsServiceURI(String jobServiceUrl) {
        return URIBuilder.toURI(jobServiceUrl + JOBS_PATH);
    }

    public URI getJobsServiceUri() {
        return jobsServiceUri;
    }
}
