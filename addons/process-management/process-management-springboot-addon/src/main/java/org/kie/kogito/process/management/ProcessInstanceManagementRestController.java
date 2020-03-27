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

package org.kie.kogito.process.management;

import org.kie.kogito.Application;
import org.kie.kogito.process.Processes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/management/processes")
public class ProcessInstanceManagementRestController extends BaseProcessInstanceManagementResource<ResponseEntity> {

    @Autowired
    public ProcessInstanceManagementRestController(Processes processes, Application application) {
        super(processes, application);
    }

    @Override
    public <R> ResponseEntity buildOkResponse(R body) {
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity badRequestResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @Override
    public ResponseEntity notFoundResponse(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @Override
    @GetMapping(value = "{processId}/instances/{processInstanceId}/error", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getInstanceInError(@PathVariable("processId") String processId,
                                             @PathVariable("processInstanceId") String processInstanceId) {
        return doGetInstanceInError(processId, processInstanceId);
    }

    @Override
    @GetMapping(value = "{processId}/instances/{processInstanceId}/nodeInstances", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getWorkItemsInProcessInstance(@PathVariable("processId") String processId, @PathVariable("processInstanceId") String processInstanceId) {
        return doGetWorkItemsInProcessInstance(processId, processInstanceId);
    }

    @Override
    @PostMapping(value = "{processId}/instances/{processInstanceId}/retrigger", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity retriggerInstanceInError(@PathVariable("processId") String processId, @PathVariable(
            "processInstanceId") String processInstanceId) {
        return doRetriggerInstanceInError(processId, processInstanceId);
    }

    @Override
    @PostMapping(value = "{processId}/instances/{processInstanceId}/skip", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity skipInstanceInError(@PathVariable("processId") String processId, @PathVariable("processInstanceId") String processInstanceId) {
        return doSkipInstanceInError(processId, processInstanceId);
    }

    @Override
    @PostMapping(value = "{processId}/instances/{processInstanceId}/nodes/{nodeId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity triggerNodeInstanceId(@PathVariable("processId") String processId, @PathVariable("processInstanceId") String processInstanceId, @PathVariable("nodeId") String nodeId) {
        return doTriggerNodeInstanceId(processId, processInstanceId, nodeId);
    }

    @Override
    @PostMapping(value = "{processId}/instances/{processInstanceId}/nodeInstances/{nodeInstanceId}", produces =
            APPLICATION_JSON_VALUE)
    public ResponseEntity retriggerNodeInstanceId(@PathVariable("processId") String processId, @PathVariable("processInstanceId") String processInstanceId, @PathVariable("nodeInstanceId") String nodeInstanceId) {
        return doRetriggerNodeInstanceId(processId, processInstanceId, nodeInstanceId);
    }

    @Override
    @DeleteMapping(value = "{processId}/instances/{processInstanceId}/nodeInstances/{nodeInstanceId}", produces =
            APPLICATION_JSON_VALUE)
    public ResponseEntity cancelNodeInstanceId(@PathVariable("processId") String processId, @PathVariable("processInstanceId") String processInstanceId, @PathVariable("nodeInstanceId") String nodeInstanceId) {
        return doCancelNodeInstanceId(processId, processInstanceId, nodeInstanceId);
    }

    @Override
    @DeleteMapping(value = "{processId}/instances/{processInstanceId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity cancelProcessInstanceId(@PathVariable("processId") String processId, @PathVariable("processInstanceId") String processInstanceId) {
        return doCancelProcessInstanceId(processId, processInstanceId);
    }
}
