/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package $Package$;

@javax.enterprise.context.ApplicationScoped
public class Processes implements org.kie.kogito.process.Processes {

    @javax.inject.Inject
    javax.enterprise.inject.Instance<org.kie.kogito.process.Process> processes;

    private java.util.Map<String, org.kie.kogito.process.Process> mappedProcesses = new java.util.HashMap<>();

    @javax.annotation.PostConstruct
    public void setup() {
        for (org.kie.kogito.process.Process process : processes) {
            mappedProcesses.put(process.id(), process);
        }
    }

    public org.kie.kogito.process.Process processById(String processId) {
        return mappedProcesses.get(processId);
    }

    public java.util.Collection<String> processIds() {
        return mappedProcesses.keySet();
    }
}
