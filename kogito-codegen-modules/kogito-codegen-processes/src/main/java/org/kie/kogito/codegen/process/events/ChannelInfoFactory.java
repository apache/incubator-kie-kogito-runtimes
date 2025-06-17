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
package org.kie.kogito.codegen.process.events;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;

public class ChannelInfoFactory {

    private ChannelInfoFactory() {
    }

    private static final String OUTGOING_PREFIX = "mp.messaging.outgoing.";
    private static final String INCOMING_PREFIX = "mp.messaging.incoming.";

    private static final String KOGITO_MESSAGING_PREFIX = "kogito.addon.messaging.";
    private static final String KOGITO_INCOMING_PREFIX = "kogito.addon.messaging.incoming.";
    private static final String KOGITO_OUTGOING_PREFIX = "kogito.addon.messaging.outgoing.";
    private static final String INCOMING_TRIGGER = KOGITO_INCOMING_PREFIX + "trigger.";
    private static final String OUTGOING_TRIGGER = KOGITO_OUTGOING_PREFIX + "trigger.";
    public static final String INCOMING_DEFAULT_CHANNEL = KOGITO_INCOMING_PREFIX + "defaultName";
    public static final String OUTGOING_DEFAULT_CHANNEL = KOGITO_OUTGOING_PREFIX + "defaultName";

    private static final String CLOUD_EVENT_MODE = KOGITO_OUTGOING_PREFIX + "cloudEventMode";

    private static final String MARSHALLER_PREFIX = KOGITO_MESSAGING_PREFIX + "marshaller.";
    private static final String UNMARSHALLLER_PREFIX = KOGITO_MESSAGING_PREFIX + "unmarshaller.";
    private static final String KOGITO_EMITTER_PREFIX = KOGITO_MESSAGING_PREFIX + "emitter.";

    public static ChannelInfo newChannelInfo(KogitoBuildContext config, String name, String prefix, boolean isInput, String defaultChannelName, Map<String, Collection<String>> triggers) {
        return new ChannelInfo(name,
                triggers.getOrDefault(name, Collections.singleton(name)),
                getClassName(config.getApplicationProperty(getPropertyName(prefix, name, "value." + (isInput ? "deserializer" : "serializer")), String.class)),
                isInput,
                name.equals(defaultChannelName),
                config.getApplicationProperty((isInput ? UNMARSHALLLER_PREFIX : MARSHALLER_PREFIX) + name),
                cloudEventMode(config, name, name));
    }

    private static Optional<CloudEventMode> cloudEventMode(KogitoBuildContext config, String name, String property) {
        if (!config.getApplicationProperty("kogito.messaging.as-cloudevents", Boolean.class).orElse(true)) {
            return Optional.empty();
        }
        Optional<CloudEventMode> cloudEventMode = getCloudEventMode(config, CLOUD_EVENT_MODE + "." + name);
        if (cloudEventMode.isPresent()) {
            return cloudEventMode;
        }
        cloudEventMode = getCloudEventMode(config, CLOUD_EVENT_MODE);
        if (cloudEventMode.isPresent()) {
            return cloudEventMode;
        }
        // if no config, infer default from connector type
        Optional<String> connector = config.getApplicationProperty(property);
        if (connector.isEmpty()) {
            return Optional.of(CloudEventMode.STRUCTURED);
        }
        return Optional.of(connector.get().equals("quarkus-http") ? CloudEventMode.BINARY : CloudEventMode.STRUCTURED);
    }

    private static Optional<CloudEventMode> getCloudEventMode(KogitoBuildContext config, String propName) {
        return config.getApplicationProperty(propName).map(String::toUpperCase).map(CloudEventMode::valueOf);
    }

    private static String getClassName(Optional<String> serializerClassName) {
        if (serializerClassName.isPresent()) {
            String value = serializerClassName.get();
            int indexOf = value.lastIndexOf(".");
            if (indexOf >= 0) {
                value = value.substring(indexOf + 1);
            }
            // Checking for StringSerializer or ByteArraySerializer in order to keep backward compatibility 
            if (value.startsWith("String")) {
                return "String";
            } else if (value.startsWith("ByteArray")) {
                return "byte[]";
            }
        }
        // Default behavior is to use the object marshaller. 
        return "Object";
    }

    private static final String getPropertyName(String prefix, String name, String suffix) {
        return prefix + name + "." + suffix;
    }
}
