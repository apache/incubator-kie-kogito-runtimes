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
package org.kie.kogito.services.event.impl;

import java.util.HashSet;
import java.util.Set;

import org.kie.kogito.event.ChannelInfo;
import org.kie.kogito.event.ChannelResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelResolver implements ChannelResolver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultChannelResolver.class);

    protected Set<ChannelInfo> inputChannels = new HashSet<>();
    protected Set<ChannelInfo> outputChannels = new HashSet<>();

    protected static final void addChannel(Set<ChannelInfo> channels, String beanName, String channelName) {
        ChannelInfo channelInfo = new ChannelInfo(beanName, channelName);
        if (!channels.contains(channelInfo)) {
            channels.add(channelInfo);
        } else {
            logger.warn("Channel {} already added. Consider using a different trigger name", channelInfo);
        }
    }

    @Override
    public Set<ChannelInfo> getOutputChannels() {
        return outputChannels;
    }

    @Override
    public Set<ChannelInfo> getInputChannels() {
        return inputChannels;
    }
}
