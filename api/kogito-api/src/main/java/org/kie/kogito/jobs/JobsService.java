/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.jobs;

/**
 * JobsService provides an entry point for working with different types of jobs
 * that are meant by default to run in background. 
 *
 */
public interface JobsService {

    /**
     * Schedules process job that is responsible for starting new process instances
     * based on the given description.
     * @param description defines what kind of process should be started upon expiration time
     * @return returns unique id of the job
     */
    String scheduleProcessJob(ProcessJobDescription description);
    
    /**
     * Schedules process instance related job that will signal exact same process instance
     * upon expiration time.
     * @param description defines the context of the process instance that should be signaled
     * @return returns unique id of the job
     */
    String scheduleProcessInstanceJob(ProcessInstanceJobDescription description);
    
    /**
     * Cancels given job
     * @param id unique id of the job
     * @return return true if the cancellation was successful, otherwise false 
     */
    boolean cancelJob(String id);
}
