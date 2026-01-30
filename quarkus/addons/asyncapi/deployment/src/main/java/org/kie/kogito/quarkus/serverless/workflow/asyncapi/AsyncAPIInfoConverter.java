
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
package org.kie.kogito.quarkus.serverless.workflow.asyncapi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.kie.kogito.serverless.workflow.asyncapi.AsyncChannelInfo;
import org.kie.kogito.serverless.workflow.asyncapi.AsyncInfo;
import org.kie.kogito.serverless.workflow.asyncapi.AsyncInfoConverter;

// Updated to AsyncAPI v3.0.0 (from v2.6.0) as part of Quarkus 3.27.2 upgrade
import com.asyncapi.v3._0_0.model.AsyncAPI;
import com.asyncapi.v3._0_0.model.channel.Channel;
import com.asyncapi.v3._0_0.model.operation.Operation;
import com.asyncapi.v3._0_0.model.operation.OperationAction;

import io.quarkiverse.asyncapi.config.AsyncAPIRegistry;

public class AsyncAPIInfoConverter implements AsyncInfoConverter {

    private final AsyncAPIRegistry registry;

    public AsyncAPIInfoConverter(AsyncAPIRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Optional<AsyncInfo> apply(String id) {
        return registry.getAsyncAPI(id).map(AsyncAPIInfoConverter::from);
    }

    private static AsyncInfo from(AsyncAPI asyncApi) {
        Map<String, AsyncChannelInfo> map = new HashMap<>();

        // AsyncAPI v3.0.0 migration: In v3, channels and operations are separate top-level objects
        // v2: channels contained publish/subscribe operations directly
        // v3: operations reference channels via $ref, and channels have addresses
        // Build a helper map: channelId -> address (topic/path)
        Map<String, String> channelIdToAddress = new HashMap<>();
        if (asyncApi.getChannels() != null) {
            for (Entry<String, Object> ch : asyncApi.getChannels().entrySet()) {
                String channelId = ch.getKey();
                if (ch.getValue() instanceof Channel) {
                    Channel channel = (Channel) ch.getValue();
                    // In v3, address holds the actual path/topic (key is just an ID)
                    String address = Optional.ofNullable(channel.getAddress()).orElse(channelId);
                    channelIdToAddress.put(channelId, address);
                }
            }
        }

        // Iterate operations and map operation key -> AsyncChannelInfo
        if (asyncApi.getOperations() != null) {
            for (Entry<String, Object> opEntry : asyncApi.getOperations().entrySet()) {
                String opKey = opEntry.getKey();
                if (!(opEntry.getValue() instanceof Operation)) {
                    continue;
                }
                Operation op = (Operation) opEntry.getValue();

                // In v3.0.0, the operation key is the operation identifier
                String operationId = opKey;

                // Resolve the referenced channel ID from "$ref: #/channels/<id>"
                String channelId = null;
                if (op.getChannel() != null) {
                    String ref = op.getChannel().getRef(); // expected form
                    if (ref != null && ref.contains("/")) {
                        channelId = ref.substring(ref.lastIndexOf('/') + 1);
                    }
                }

                String address = channelId != null ? channelIdToAddress.get(channelId) : null;
                OperationAction action = op.getAction();

                if (operationId != null && address != null && action != null) {
                    // AsyncAPI v3.0.0: action is now OperationAction enum (SEND/RECEIVE) instead of v2's publish/subscribe
                    boolean publish = action == OperationAction.SEND; // send -> we publish, receive -> we consume
                    String channelName = publish ? address + "_out" : address;
                    map.putIfAbsent(operationId, new AsyncChannelInfo(channelName, publish));
                }
            }
        }

        return new AsyncInfo(map);
    }
}