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
package org.kie.kogito.codegen.process;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jbpm.compiler.canonical.TriggerMetaData;
import org.jbpm.compiler.canonical.TriggerMetaData.TriggerType;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.event.KogitoEventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelMappingStrategy implements ChannelMappingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DefaultChannelMappingStrategy.class);

    private static final String OUTGOING_PREFIX = "mp.messaging.outgoing.";
    private static final String INCOMING_PREFIX = "mp.messaging.incoming.";

    private static final String INCOMING_PROP_NAME = getPropertyName(INCOMING_PREFIX, KogitoEventStreams.INCOMING);
    private static final String OUTGOING_PROP_NAME = getPropertyName(OUTGOING_PREFIX, KogitoEventStreams.OUTGOING);

    @Override
    public Map<TriggerMetaData, String> getChannelMapping(KogitoBuildContext context,
            Collection<TriggerMetaData> metadata) {
        Map<TriggerMetaData, String> map = new LinkedHashMap<>();
        Set<TriggerMetaData> missingTriggers = new HashSet<>();
        for (TriggerMetaData trigger : metadata) {
            if (trigger.getType() == TriggerType.ConsumeMessage || trigger.getType() == TriggerType.ProduceMessage) {
                String channel = getChannel(trigger, context);
                if (channel != null) {
                    map.put(trigger, channel);
                } else {
                    missingTriggers.add(trigger);
                }
            } else {
                logger.debug("trigger {} is not consumer, not producer, ignoring", trigger);
            }
        }
        handleMissing(map, missingTriggers);
        return map;
    }

    protected void handleMissing(Map<TriggerMetaData, String> map, Collection<TriggerMetaData> missingTriggers) {
        if (!missingTriggers.isEmpty()) {
            String defaultIncoming = KogitoEventStreams.INCOMING;
            String defaultOutgoing = KogitoEventStreams.OUTGOING;
            for (Entry<TriggerMetaData, String> entry : map.entrySet()) {
                if (entry.getKey().getType() == TriggerType.ConsumeMessage) {
                    defaultIncoming = entry.getValue();
                } else {
                    defaultOutgoing = entry.getValue();
                }
            }
            for (TriggerMetaData missingTrigger : missingTriggers) {
                String channel = missingTrigger.getType() == TriggerType.ConsumeMessage ? defaultIncoming : defaultOutgoing;
                map.put(missingTrigger, channel);
                logger.warn("Cannot find mapping for trigger {}. Mapping trigger to  {}", missingTrigger, channel);
            }
        }

    }

    private String getChannel(TriggerMetaData trigger, KogitoBuildContext context) {
        if (trigger.getType() == TriggerType.ConsumeMessage) {
            return getChannel(trigger, context, INCOMING_PREFIX, INCOMING_PROP_NAME, KogitoEventStreams.INCOMING);
        } else {
            return getChannel(trigger, context, OUTGOING_PREFIX, OUTGOING_PROP_NAME, KogitoEventStreams.OUTGOING);
        }
    }

    private static String getChannel(TriggerMetaData trigger, KogitoBuildContext context, String prefix, String defaultChannel, String defaultName) {
        if (context.getApplicationProperty(getPropertyName(prefix, trigger.getName())).isPresent()) {
            return trigger.getName();
        } else if (context.getApplicationProperty(defaultChannel).isPresent()) {
            return defaultName;
        } else {
            return null;
        }
    }

    private static final String getPropertyName(String prefix, String name) {
        return prefix + name + ".connector";
    }
}
