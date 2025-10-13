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
import java.util.Optional;

import org.drools.codegen.common.GeneratedFile;
import org.drools.codegen.common.GeneratedFileType;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.core.AbstractGenerator;
import org.kie.kogito.codegen.core.utils.CodegenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventCodegen extends AbstractGenerator {
    private static final GeneratedFileType EVENT_PRODUCER_TYPE = GeneratedFileType.of("EVENT_PRODUCER", GeneratedFileType.Category.SOURCE);
    private static final GeneratedFileType EVENT_CONSUMER_TYPE = GeneratedFileType.of("EVENT_CONSUMER", GeneratedFileType.Category.SOURCE);

    private static Logger LOGGER = LoggerFactory.getLogger(EventCodegen.class);

    private Collection<ChannelInfo> channels;

    public EventCodegen(KogitoBuildContext context) {
        super(context, "messaging");
        channels = ChannelMappingStrategy.getChannelMapping(context);
        channels.forEach(channel -> LOGGER.info("Generate channel endpoint {}", channel));
    }

    @Override
    public Optional<ApplicationSection> section() {
        return Optional.empty();
    }

    @Override
    public boolean isEmpty() {
        return channels.isEmpty();
    }

    @Override
    protected Collection<GeneratedFile> internalGenerate() {
        return generateEvents(channels);
    }

    private Collection<GeneratedFile> generateEvents(Collection<ChannelInfo> channelsInfo) {
        Collection<GeneratedFile> generatedFiles = new ArrayList<>();
        boolean isTxEnabeld = CodegenUtil.isTransactionEnabled(this, context());
        for (ChannelInfo channelInfo : channelsInfo) {
            GeneratedFileType type = null;
            ClassGenerator classGenerator = null;
            LOGGER.debug("Generate channel endpoint {}", channelInfo);
            if (channelInfo.isInput()) {
                type = EVENT_CONSUMER_TYPE;
                classGenerator = new EventReceiverGenerator(context(), channelInfo, isTxEnabeld);
            } else {
                type = EVENT_PRODUCER_TYPE;
                classGenerator = new EventEmitterGenerator(context(), channelInfo, isTxEnabeld);
            }

            generatedFiles.add(new GeneratedFile(type, classGenerator.getPath(), classGenerator.getCode()));
        }
        return generatedFiles;
    }
}
