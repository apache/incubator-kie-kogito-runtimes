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
package org.kie.kogito.codegen.process.events.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.process.events.CloudEventMode;
import org.kie.kogito.event.KogitoEventStreams;

public class QuarkusChannelInfoProcessor extends ChannelInfoProcessor {

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

    public QuarkusChannelInfoProcessor(KogitoBuildContext context) {
        super(context);
    }

    @Override
    public Map<String, String> filter(Map<String, String> applicationProperties) {
        return applicationProperties.entrySet().stream().filter(this::filterProperty).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean filterProperty(Map.Entry<String, String> property) {
        return property.getKey().startsWith(INCOMING_PREFIX) || property.getKey().startsWith(OUTGOING_PREFIX);
    }

    @Override
    public List<ChannelInfo> toChannelInfo(Map<String, String> channelProperties) {
        List<ChannelInfo> result = new ArrayList<>();
        final String defaultIncomingChannel = channelProperties.getOrDefault(INCOMING_DEFAULT_CHANNEL, KogitoEventStreams.INCOMING);
        final String defaultOutgoingChannel = channelProperties.getOrDefault(OUTGOING_DEFAULT_CHANNEL, KogitoEventStreams.OUTGOING);
        for (String property : channelProperties.keySet().stream().filter(e -> e.endsWith(".topic")).toList()) {
            if (property.startsWith(INCOMING_PREFIX)) {
                result.add(newChannelInfo(getKogitoBuildContext(), property, INCOMING_PREFIX, true, defaultIncomingChannel));
            } else if (property.startsWith(OUTGOING_PREFIX)) {
                result.add(newChannelInfo(getKogitoBuildContext(), property, OUTGOING_PREFIX, false, defaultOutgoingChannel));
            }
        }

        return result;
    }

    public ChannelInfo newChannelInfo(KogitoBuildContext config, String property, String prefix, boolean isInput, String defaultChannelName) {
        String name = property.substring(prefix.length(), property.lastIndexOf('.'));
        return new ChannelInfo(name,
                getClassName(config.getApplicationProperty(getPropertyName(prefix, name, "value." + (isInput ? "deserializer" : "serializer")), String.class)),
                isInput,
                name.equals(defaultChannelName),
                config.getApplicationProperty((isInput ? UNMARSHALLLER_PREFIX : MARSHALLER_PREFIX) + name),
                cloudEventMode(config, name, name));
    }

    private Optional<CloudEventMode> cloudEventMode(KogitoBuildContext config, String name, String property) {
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

    private Optional<CloudEventMode> getCloudEventMode(KogitoBuildContext config, String propName) {
        return config.getApplicationProperty(propName).map(String::toUpperCase).map(CloudEventMode::valueOf);
    }

    private String getClassName(Optional<String> serializerClassName) {
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

    private final String getPropertyName(String prefix, String name, String suffix) {
        return prefix + name + "." + suffix;
    }
}
