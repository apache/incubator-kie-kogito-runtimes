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
package org.kie.kogito.addon.cloudevents.quarkus;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.eclipse.microprofile.config.ConfigProvider;

/*
 * Unused, provided just as example of different ways to obtain the channels
 */
public class PropertyChannelResolver implements ChannelResolver {

    private static final Pattern OUTGOING_PATTERN = Pattern.compile("mp\\.messaging\\.outgoing\\.([a-zA-Z].*)\\..*");
    private static final Pattern INCOMING_PATTERN = Pattern.compile("mp\\.messaging\\.incoming\\.([a-zA-Z].*)\\..*");
    private Collection<String> outputChannels;
    private Collection<String> inputChannels;

    @PostConstruct
    private void init() {
        inputChannels = new HashSet<>();
        outputChannels = new HashSet<>();
        for (String propertyName : ConfigProvider.getConfig().getPropertyNames()) {
            Matcher matcher = OUTGOING_PATTERN.matcher(propertyName);
            if (matcher.matches()) {
                outputChannels.add(matcher.group(1));
            } else {
                matcher = INCOMING_PATTERN.matcher(propertyName);
                if (matcher.matches()) {
                    inputChannels.add(matcher.group(1));
                }
            }
        }
    }

    @Override
    public Collection<String> getOuputChannels() {
        return outputChannels;
    }

    @Override
    public Collection<String> getInputChannels() {
        return inputChannels;
    }
}
