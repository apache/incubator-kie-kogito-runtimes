package org.kie.kogito.codegen.process.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.drools.codegen.common.GeneratedFile;
import org.drools.codegen.common.GeneratedFileType;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.core.AbstractGenerator;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.process.util.CodegenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventCodegen extends AbstractGenerator {

    private static Logger LOGGER = LoggerFactory.getLogger(EventCodegen.class);

    private Collection<ChannelInfo> channels;

    public EventCodegen(KogitoBuildContext context) {
        super(context, "messaging");
        channels = ChannelMappingStrategy.getChannelMapping(context);
        LOGGER.info("Generate channel endpoint {}", channels);
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
                type = ProcessCodegen.MESSAGE_CONSUMER_TYPE;
                classGenerator = new EventReceiverGenerator(context(), channelInfo, isTxEnabeld);
            } else {
                type = ProcessCodegen.MESSAGE_PRODUCER_TYPE;
                classGenerator = new EventEmitterGenerator(context(), channelInfo, isTxEnabeld);
            }

            generatedFiles.add(new GeneratedFile(type, classGenerator.getPath(), classGenerator.getCode()));
        }
        return generatedFiles;
    }
}
