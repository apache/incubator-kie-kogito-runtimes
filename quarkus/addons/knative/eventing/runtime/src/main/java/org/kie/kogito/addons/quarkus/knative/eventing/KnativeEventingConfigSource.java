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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.kie.kogito.event.KogitoEventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the default configuration for a Kogito project that uses Knative Eventing as underling event platform
 */
public class KnativeEventingConfigSource implements ConfigSource {

    private static final Map<String, String> configuration = new HashMap<>();

    private static final String URL_CONFIG = "mp.messaging.outgoing." + KogitoEventStreams.OUTGOING + ".url";

    /**
     * Default Knative Sink for local dev environments. Just a default endpoint, nothing in particular.
     * Users can then configure their local sinks to this port.
     */
    private static final String DEFAULT_SINK_URL = "http://localhost:9090";

    /**
     * Environment variable injected by Knative
     */
    public static final String K_SINK = "K_SINK";
    private static final Logger LOGGER = LoggerFactory.getLogger(KnativeEventingConfigSource.class);

    static {
        configuration.put("mp.messaging.outgoing." + KogitoEventStreams.OUTGOING + ".connector", "quarkus-http");
        // add the default configuration to fallback to a placeholder since the underlying connector will fail on
        // bootstrap if either the env var is not defined or the URL is not valid.
        // we handle the missing env var injected by knative via probe
        configuration.put(URL_CONFIG, "${K_SINK:" + DEFAULT_SINK_URL + "}");
    }

    /**
     * We only consider our config if the user has not added these properties to their project
     *
     * @see <a href="https://quarkus.io/guides/config-extending-support#example">Quarkus Config Extending Support</a>
     */
    @Override
    public int getOrdinal() {
        return Integer.MIN_VALUE;
    }

    @Override
    public Set<String> getPropertyNames() {
        return configuration.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        if (URL_CONFIG.equals(propertyName)) {
            final Optional<String> sinkUrl = ConfigProvider.getConfig().getOptionalValue(K_SINK, String.class);
            if (sinkUrl.isEmpty() || "".equals(sinkUrl.get())) {
                LOGGER.warn("{} variable is empty or don't exist. Please make sure that this service is a Knative Source or has a SinkBinding bound to it.", K_SINK);
            }
        }
        return configuration.get(propertyName);
    }

    @Override
    public String getName() {
        return KnativeEventingConfigSource.class.getSimpleName();
    }
}
