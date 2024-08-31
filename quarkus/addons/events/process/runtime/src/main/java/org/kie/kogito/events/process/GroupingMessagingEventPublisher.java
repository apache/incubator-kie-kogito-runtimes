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
package org.kie.kogito.events.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.kie.kogito.event.DataEvent;

import io.quarkus.arc.properties.IfBuildProperty;

import jakarta.inject.Singleton;

@Singleton
@IfBuildProperty(name = "kogito.events.grouping", stringValue = "true")
public class GroupingMessagingEventPublisher extends AbstractMessagingEventPublisher {

    @Override
    public void publish(DataEvent<?> event) {
        publish(Collections.singletonList(event));
    }

    @Override
    public void publish(Collection<DataEvent<?>> events) {
        Map<AbstractMessageEmitter, Collection<DataEvent<?>>> eventsByChannel = new HashMap<>();
        for (DataEvent<?> event : events) {
            getConsumer(event).ifPresent(c -> eventsByChannel.computeIfAbsent(c, k -> new ArrayList<>()).add(event));
        }
        for (Entry<AbstractMessageEmitter, Collection<DataEvent<?>>> item : eventsByChannel.entrySet()) {
            publishToTopic(item.getKey(), item.getValue());
        }
    }

}
