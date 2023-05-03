/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.event.cloudevents.utils;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.cloudevents.SpecVersion;

import static io.cloudevents.core.v1.CloudEventV1.DATACONTENTTYPE;
import static io.cloudevents.core.v1.CloudEventV1.TIME;

abstract class BaseCloudEventValidator {

    private final String specVersionAttributeName;

    private final SpecVersion supportedSpecVersion;

    BaseCloudEventValidator(String specVersionAttributeName, SpecVersion supportedSpecVersion) {
        this.specVersionAttributeName = specVersionAttributeName;
        this.supportedSpecVersion = supportedSpecVersion;
    }

    final void validateCloudEvent(Map<String, Object> cloudEvent) throws InvalidCloudEventException {
        Object specVersion = cloudEvent.get(specVersionAttributeName);

        if (specVersion == null) {
            throw new InvalidCloudEventException(List.of("Missing mandatory attribute: " + specVersionAttributeName));
        }

        if (specVersion.toString().equals(supportedSpecVersion.toString())) {
            List<String> errors = new ArrayList<>();

            CloudEventUtils.getMissingAttributes(cloudEvent)
                    .forEach(missingAttribute -> errors.add("Missing mandatory attribute: " + missingAttribute));

            errors.addAll(validateNonEmptyAttributes(cloudEvent));

            errors.addAll(validateRfc2046Attributes(cloudEvent));

            errors.addAll(validateRfc3339Attributes(cloudEvent));

            if (!errors.isEmpty()) {
                throw new InvalidCloudEventException(errors);
            }
        } else {
            throw new UnsupportedOperationException(
                    "Invalid CloudEvents specification version: " + specVersion + ". "
                            + CloudEventValidatorV03.class.getName() + " supports only CloudEvents " + supportedSpecVersion);
        }
    }

    /**
     * Validates attributes that must adhere to the format specified in <a href="https://datatracker.ietf.org/doc/html/rfc3339">RFC 3339</a>.
     *
     * @param cloudEvent the CloudEvent
     * @return a {@link List} of errors
     */
    private static List<String> validateRfc3339Attributes(Map<String, Object> cloudEvent) {
        return Stream.of(TIME)
                .filter(attribute -> {
                    Object value = cloudEvent.get(attribute);
                    return value != null && !isRfc3339Value(value);
                }).map(attribute -> attribute + " MUST adhere to the format specified in RFC 3339 (https://datatracker.ietf.org/doc/html/rfc3339).")
                .collect(Collectors.toList());
    }

    private static boolean isRfc3339Value(Object value) {
        if (value instanceof String) {
            try {
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse((String) value);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }

        return false;
    }

    /**
     * Validates attributes that must adhere to the format specified in <a href="https://datatracker.ietf.org/doc/html/rfc2046">RFC 2046</a>.
     *
     * @param cloudEvent the CloudEvent
     * @return a {@link List} of errors
     */
    private List<String> validateRfc2046Attributes(Map<String, Object> cloudEvent) {
        return Stream.of(DATACONTENTTYPE)
                .filter(attribute -> {
                    Object value = cloudEvent.get(attribute);
                    return value != null && !isRfc2046Value(value);
                }).map(attribute -> attribute + " MUST adhere to the format specified in RFC 2046 (https://datatracker.ietf.org/doc/html/rfc2046).")
                .collect(Collectors.toList());
    }

    private static boolean isRfc2046Value(Object value) {
        if (value instanceof String) {
            Pattern pattern = Pattern.compile("^[a-zA-Z]+/[a-zA-Z]+(?:[+\\-.][a-zA-Z0-9]+){0,10}$");
            Matcher matcher = pattern.matcher((String) value);
            return matcher.matches();
        }
        return false;
    }

    private List<String> validateNonEmptyAttributes(Map<String, Object> cloudEvent) {
        return getNonEmptyAttributes().stream()
                .filter(attribute -> "".equals(cloudEvent.get(attribute)))
                .map(attribute -> attribute + " must be a non-empty String.")
                .collect(Collectors.toList());
    }

    protected abstract List<String> getNonEmptyAttributes();
}
