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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.event.KogitoEventStreams;

public class ChannelMappingStrategy {

    private static List<String> standardChannels = List.of("kogito-processdefinitions-events", "kogito-processinstances-events", "kogito-usertaskinstances-events", "kogito-variables-events");

    private ChannelMappingStrategy() {
    }

    private static final String OUTGOING_PREFIX = "mp.messaging.outgoing.";
    private static final String INCOMING_PREFIX = "mp.messaging.incoming.";

    private static final String KOGITO_MESSAGING_PREFIX = "kogito.addon.messaging.";
    private static final String KOGITO_INCOMING_PREFIX = "kogito.addon.messaging.incoming.";
    private static final String KOGITO_OUTGOING_PREFIX = "kogito.addon.messaging.outgoing.";
    private static final String INCOMING_TRIGGER = KOGITO_INCOMING_PREFIX + "trigger.";
    private static final String OUTGOING_TRIGGER = KOGITO_OUTGOING_PREFIX + "trigger.";
    private static final String INCOMING_DEFAULT_CHANNEL = KOGITO_INCOMING_PREFIX + "defaultName";
    private static final String OUTGOING_DEFAULT_CHANNEL = KOGITO_OUTGOING_PREFIX + "defaultName";

    private static final String CLOUD_EVENT_MODE = KOGITO_OUTGOING_PREFIX + "cloudEventMode";

    private static final String MARSHALLER_PREFIX = KOGITO_MESSAGING_PREFIX + "marshaller.";
    private static final String UNMARSHALLLER_PREFIX = KOGITO_MESSAGING_PREFIX + "unmarshaller.";
    private static final String KOGITO_EMITTER_PREFIX = KOGITO_MESSAGING_PREFIX + "emitter.";

    public static Collection<ChannelInfo> getChannelMapping(KogitoBuildContext context) {
        Map<String, Collection<String>> inTriggers = new HashMap<>();
        Map<String, Collection<String>> outTriggers = new HashMap<>();

        for (String property : context.getApplicationProperties()) {
            if (property.startsWith(INCOMING_TRIGGER)) {
                addTrigger(context, INCOMING_TRIGGER, property, inTriggers);
            } else if (property.startsWith(OUTGOING_TRIGGER)) {
                addTrigger(context, OUTGOING_TRIGGER, property, outTriggers);
            }
        }

        Collection<ChannelInfo> result = new ArrayList<>();
        final String defaultIncomingChannel = context.getApplicationProperty(INCOMING_DEFAULT_CHANNEL, String.class).orElse(KogitoEventStreams.INCOMING);
        final String defaultOutgoingChannel = context.getApplicationProperty(OUTGOING_DEFAULT_CHANNEL, String.class).orElse(KogitoEventStreams.OUTGOING);
        for (String property : context.getApplicationProperties()) {

            if (property.startsWith(INCOMING_PREFIX) && property.endsWith(".connector")) {
                String name = property.substring(INCOMING_PREFIX.length(), property.lastIndexOf('.'));
                if (standardChannels.contains(name)) {
                    continue;
                }
                result.add(getChannelInfo(context, property, INCOMING_PREFIX, true, defaultIncomingChannel, inTriggers));
            } else if (property.startsWith(OUTGOING_PREFIX) && property.endsWith(".connector")) {
                String name = property.substring(OUTGOING_PREFIX.length(), property.lastIndexOf('.'));
                if (standardChannels.contains(name)) {
                    continue;
                }
                result.add(getChannelInfo(context, property, OUTGOING_PREFIX, false, defaultOutgoingChannel, outTriggers));
            }
        }
        return result;
    }

    private static void addTrigger(KogitoBuildContext context, String prefix, String property, Map<String, Collection<String>> triggers) {
        String channelName = context.getApplicationProperty(property, String.class).orElse(null);
        String triggerName = property.substring(prefix.length());
        triggers.computeIfAbsent(channelName, ChannelMappingStrategy::initTriggers).add(triggerName);
    }

    private static Collection<String> initTriggers(String channelName) {
        Collection<String> result = new HashSet<>();
        result.add(channelName);
        return result;
    }

    private static ChannelInfo getChannelInfo(KogitoBuildContext context, String property, String prefix, boolean isInput, String defaultChannelName, Map<String, Collection<String>> triggers) {
        String name = property.substring(prefix.length(), property.lastIndexOf('.'));
        return new ChannelInfo(name,
                triggers.getOrDefault(name, Collections.singleton(name)),
                getClassName(context.getApplicationProperty(getPropertyName(prefix, name, "value." + (isInput ? "deserializer" : "serializer")), String.class)),
                isInput,
                name.equals(defaultChannelName),
                context.getApplicationProperty((isInput ? UNMARSHALLLER_PREFIX : MARSHALLER_PREFIX) + name, String.class),
                cloudEventMode(context, name, property));
    }

    private static Optional<CloudEventMode> cloudEventMode(KogitoBuildContext context, String name, String property) {
        if (!context.getApplicationProperty("kogito.messaging.as-cloudevents", Boolean.class).orElse(true)) {
            return Optional.empty();
        }
        Optional<CloudEventMode> cloudEventMode = getCloudEventMode(context, CLOUD_EVENT_MODE + "." + name);
        if (cloudEventMode.isPresent()) {
            return cloudEventMode;
        }
        cloudEventMode = getCloudEventMode(context, CLOUD_EVENT_MODE);
        if (cloudEventMode.isPresent()) {
            return cloudEventMode;
        }
        // if no config, infer default from connector type
        return context.getApplicationProperty(property, String.class).map(ChannelMappingStrategy::toCloudEventMode);
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

    private static CloudEventMode toCloudEventMode(String connector) {
        return connector.equals("quarkus-http") ? CloudEventMode.BINARY : CloudEventMode.STRUCTURED;
    }

    private static Optional<CloudEventMode> getCloudEventMode(KogitoBuildContext context, String propName) {
        return context.getApplicationProperty(propName).map(String::toUpperCase).map(CloudEventMode::valueOf);
    }

    private static final String getPropertyName(String prefix, String name, String suffix) {
        return prefix + name + "." + suffix;
    }
}
