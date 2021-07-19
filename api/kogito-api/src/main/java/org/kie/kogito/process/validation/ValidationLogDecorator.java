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
package org.kie.kogito.process.validation;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationLogDecorator extends ValidationDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationLogDecorator.class);

    public ValidationLogDecorator(ValidationException exception) {
        super(exception);
    }

    public static ValidationLogDecorator of(ValidationException exception) {
        return new ValidationLogDecorator(exception);
    }

    @Override
    public ValidationLogDecorator decorate() {
        String message = exception.getErrors()
                .stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.joining("\n - ", " - ", ""));
        LOGGER.error("Invalid process: '{}'. Found errors:\n{}\n", exception.getProcessId(), message);
        return this;
    }
}
