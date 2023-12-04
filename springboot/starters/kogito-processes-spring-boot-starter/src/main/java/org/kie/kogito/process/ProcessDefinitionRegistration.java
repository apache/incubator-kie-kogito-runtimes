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
package org.kie.kogito.process;

import org.kie.kogito.Application;
import org.kie.kogito.services.registry.ProcessDefinitionEventRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcessDefinitionRegistration implements InitializingBean {
    Processes processes;
    ProcessDefinitionEventRegistry processDefinitionRegistry;

    @Autowired
    public ProcessDefinitionRegistration(Application application, @Value("kogito.service.url") String serviceUrl, @Autowired(required = false) Processes processes) {
        this.processes = processes;
        this.processDefinitionRegistry = new ProcessDefinitionEventRegistry(application, serviceUrl);
    }

    @Override
    public void afterPropertiesSet() {
        if (processes != null) {
            processDefinitionRegistry.register(processes);
        }
    }
}
