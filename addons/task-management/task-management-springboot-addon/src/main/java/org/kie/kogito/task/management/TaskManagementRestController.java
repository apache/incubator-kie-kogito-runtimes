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
package org.kie.kogito.task.management;

import java.util.List;

import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.workitem.Policies;
import org.kie.kogito.task.management.service.TaskInfo;
import org.kie.kogito.task.management.service.TaskManagementOperations;
import org.kie.kogito.task.management.service.TaskManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/management/processes")
public class TaskManagementRestController {

    TaskManagementOperations taskService;

    @Autowired
    public TaskManagementRestController(Processes processes, ProcessConfig processConfig) {
        this.taskService = new TaskManagementService(processes, processConfig);
    }

    @PutMapping(value = "{processId}/instances/{processInstanceId}/tasks/{taskId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateTask(@PathVariable("processId") String processId,
            @PathVariable("processInstanceId") String processInstanceId,
            @PathVariable("taskId") String taskId,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "group", required = false) List<String> groups,
            @RequestBody TaskInfo taskInfo) {
        taskService.updateTask(processId, processInstanceId, taskId, taskInfo, true, Policies.of(user, groups));
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "{processId}/instances/{processInstanceId}/tasks/{taskId}", produces = APPLICATION_JSON_VALUE)
    public TaskInfo partialUpdateTask(@PathVariable("processId") String processId,
            @PathVariable("processInstanceId") String processInstanceId,
            @PathVariable("taskId") String taskId,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "group", required = false) List<String> groups,
            @RequestBody TaskInfo taskInfo) {
        return taskService.updateTask(processId, processInstanceId, taskId, taskInfo, false, Policies.of(user, groups));

    }

    @GetMapping(value = "{processId}/instances/{processInstanceId}/tasks/{taskId}", produces = APPLICATION_JSON_VALUE)
    public TaskInfo getTask(@PathVariable("processId") String processId,
            @PathVariable("processInstanceId") String processInstanceId,
            @PathVariable("taskId") String taskId,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "group", required = false) List<String> groups) {
        return taskService.getTask(processId, processInstanceId, taskId, Policies.of(user, groups));
    }
}
