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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.kie.kogito.services.event.InputTriggerAware;
import org.kie.kogito.services.event.OutputTriggerAware;

@ApplicationScoped
public class ProcessChannelResolver implements ChannelResolver {

    @Inject
    private Instance<InputTriggerAware> inputChannelsProvider;
    @Inject
    private Instance<OutputTriggerAware> outputChannelsProvider;
    private Collection<String> inputChannels;
    private Collection<String> outputChannels;

    @PostConstruct
    private void init() {
        inputChannels = new HashSet<>();
        inputChannelsProvider.forEach(instance -> inputChannels.add(instance.getInputTrigger()));
        outputChannels = new HashSet<>();
        outputChannelsProvider.forEach(instance -> outputChannels.add(instance.getOutputTrigger()));
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
