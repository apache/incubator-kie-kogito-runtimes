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
package org.kie.kogito.addons.quarkus.knative.serving.customfunctions;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import io.cloudevents.SpecVersion;
import io.cloudevents.core.v1.CloudEventV1;
import io.vertx.core.json.JsonObject;

import static java.util.function.Predicate.not;

@ApplicationScoped
class CloudEventValidator {

    /**
     * Checks if the specified CloudEvent contains all mandatory attributes set.
     *
     * @param cloudEvent the CloudEvent to be validated
     * @return an {@link Optional} containing the error message or an empty Optional in case the CloudEvent is valid.
     */
    Optional<String> validateCloudEvent(JsonObject cloudEvent) {
        Set<String> mandatoryAttributes = SpecVersion.parse(cloudEvent.getString(CloudEventV1.SPECVERSION, "1.0"))
                .getMandatoryAttributes();

        List<String> missingAttributes = mandatoryAttributes.stream()
                .filter(not(cloudEvent::containsKey))
                .collect(Collectors.toList());

        if (!missingAttributes.isEmpty()) {
            return Optional.of("Invalid CloudEvent. The following mandatory attributes are missing: "
                    + String.join(", ", missingAttributes));
        } else {
            return Optional.empty();
        }
    }
}
