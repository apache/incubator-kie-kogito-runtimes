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

package org.kie.kogito.explainability;

import org.kie.kogito.Application;
import org.kie.kogito.explainability.model.PredictInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/predict")
public class SpringBootExplainableResource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootExplainableResource.class);
    private static final ExplainabilityService explainabilityService = ExplainabilityService.INSTANCE;

    private final Application application;

    @Autowired
    public SpringBootExplainableResource(Application application) {
        this.application = application;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> predict(List<PredictInput> inputs) {
        try {
            return ResponseEntity.ok(explainabilityService.processRequest(application, inputs));
        } catch (Exception e) {
            LOGGER.warn("An Exception occurred processing the predict request", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
