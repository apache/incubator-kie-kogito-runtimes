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

package org.kie.kogito.jobs.management.springboot;


import javax.annotation.PostConstruct;

import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.jobs.ProcessInstanceJobDescription;
import org.kie.kogito.jobs.ProcessJobDescription;
import org.kie.kogito.jobs.api.Job;
import org.kie.kogito.jobs.api.JobBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class SpringRestJobsService implements JobsService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringRestJobsService.class);
    
    @Value("${kogito.jobs-service.url}")
    String jobServiceUrl;
    
    @Value("${kogito.service.url}")
    String callbackEndpoint;
    
    @Autowired(required=false)
    private RestTemplate restTemplate;

    @PostConstruct
    public void initialize() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            LOGGER.debug("No RestTemplate found, creating a default one");
        }
    }

    @Override
    public String scheduleProcessJob(ProcessJobDescription description) {
       
        throw new UnsupportedOperationException("Scheduling for process jobs is not yet implemented");
    }

    @Override
    public String scheduleProcessInstanceJob(ProcessInstanceJobDescription description) {
        String callback = callbackEndpoint + "/management/jobs/" + description.processId() +"/instances/" + description.processInstanceId() +"/timers/" + description.id();
        LOGGER.debug("Job to be scheduled {} with callback URL {}", description, callback);
        Job job = JobBuilder.builder()
                .id(description.id())
                .expirationTime(description.expirationTime().get())
                .priority(0)
                .callbackEndpoint(callback)
                .processId(description.processId())
                .processInstanceId(description.processInstanceId())
                .rootProcessId(description.rootProcessId())
                .rootProcessInstanceId(description.rootProcessInstanceId())
                .build();

        
        ResponseEntity<String> result = restTemplate.postForEntity(jobServiceUrl + "/job", job, String.class);
        if (result.getStatusCode().ordinal() == 200) {
            LOGGER.debug("Creating of the job {} done with status code {} ", job, result.getStatusCode());
        }
        return job.getId();
    }

    @Override
    public boolean cancelJob(String id) {
        
        try {
            restTemplate.delete(jobServiceUrl + "/job/{id}", id);        
            
            return true;
        } catch (RestClientException e) {
            LOGGER.debug("Exceltion thrown during canceling of job {}", id, e);
            return false;
        }
    }

}
