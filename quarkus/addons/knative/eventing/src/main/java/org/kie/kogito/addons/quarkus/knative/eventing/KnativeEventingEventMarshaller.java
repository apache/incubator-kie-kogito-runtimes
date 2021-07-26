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
package org.kie.kogito.addons.quarkus.knative.eventing;

import javax.enterprise.context.ApplicationScoped;

import org.kie.kogito.services.event.impl.DefaultEventMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ApplicationScoped
public class KnativeEventingEventMarshaller extends DefaultEventMarshaller {

    /**
     * @see <a href="https://knative.dev/docs/developer/eventing/sources/sinkbinding/reference/#cloudevent-overrides">Knative Eventing SinkBinding - CloudEvent Overrides</a>
     */
    public static final String K_CE_OVERRIDES = "K_CE_OVERRIDES";
    private static final Logger LOGGER = LoggerFactory.getLogger(KnativeEventingEventMarshaller.class);

    public KnativeEventingEventMarshaller() {
        super();
    }

    public KnativeEventingEventMarshaller(final ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public <T> String marshall(T event) {
        final String defaultPayload = super.marshall(event);
        final JsonNode ceOverrides = this.readEnvCeOverrides();
        if (ceOverrides == null) {
            return defaultPayload;
        }
        try {
            final ObjectNode payloadNode = (ObjectNode) this.getMapper().readTree(defaultPayload);
            return getMapper().writeValueAsString(payloadNode.setAll((ObjectNode) ceOverrides));
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to override CloudEvents extensions with K_CE_OVERRIDES value", e);
            return defaultPayload;
        }
    }

    // visible for testing, don't make it public
    JsonNode readEnvCeOverrides() {
        final String ceOverridesValue = System.getenv(K_CE_OVERRIDES);
        if (ceOverridesValue == null || "".equals(ceOverridesValue)) {
            return null;
        }
        try {
            final CeOverrides ceOverrides = this.getMapper().readValue(ceOverridesValue, CeOverrides.class);
            return this.getMapper().valueToTree(ceOverrides.getExtensions());
        } catch (JsonProcessingException e) {
            LOGGER.warn("The variable {} doesn't have a valid JSON value: {}. Skipping override.", K_CE_OVERRIDES, e.getMessage());
            return null;
        }
    }

}
