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
package com.myspace.demo;

import java.util.List;

import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.Sig;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

public class $Type$Resource {

    Process<$Type$> process;
    @PostMapping(value = "/$signalName$", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<$Type$Output> signalProcess(@RequestHeader HttpHeaders httpHeaders,
                                                      @RequestParam(value = "businessKey", required = false) String businessKey,
                                                      @RequestBody(required = false) $signalType$ data,
                                                      UriComponentsBuilder uriComponentsBuilder) {

        $Type$ model = new $Type$();
        model.set$SetModelMethodName$(data);

        ProcessInstance<$Type$> pi = this.processService.createProcessInstance(process,
                                                                               businessKey,
                                                                               model,
                                                                               httpHeaders,
                                                                               httpHeaders.getOrEmpty("X-KOGITO-StartFromNode").stream().findFirst().orElse(null),
                                                                               "$signalName$",
                                                                               httpHeaders.getOrEmpty("X-KOGITO-ReferenceId").stream().findFirst().orElse(null),
                                                                               null);

        return ResponseEntity.created(uriComponentsBuilder.path("/$name$/{id}").buildAndExpand(pi.id()).toUri())
                .body(pi.checkError().variables().toModel());
    }

    @PostMapping(value = "/{id}/$signalPath$", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public $Type$Output signalInstance(@PathVariable("id") final String id, final @RequestBody(required = false) $signalType$ data) {
        return processService.signalProcessInstance(process, id, data, "$signalName$")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}