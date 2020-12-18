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

package org.kie.kogito.integrationtests;

import org.springframework.beans.factory.annotation.Autowired;
import org.kie.kogito.Application;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.ProcessConfig;

public class SpringInjectProcesses {

    @Autowired
    public SpringInjectProcesses(Processes processes, Application application) {
        if(processes != application.get(Processes.class)) {
            throw new IllegalStateException("Processes should be injectable and same as instance application.get(Processes.class)");
        }
        if(application.config().get(ProcessConfig.class) == null) {
            throw new IllegalStateException("ProcessConfig not available");
        }
    }
}
