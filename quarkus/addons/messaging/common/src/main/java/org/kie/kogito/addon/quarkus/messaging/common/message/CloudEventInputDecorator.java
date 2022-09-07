/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addon.quarkus.messaging.common.message;

import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.kie.kogito.event.process.ProcessDataEvent;

import io.cloudevents.SpecVersion;
import io.smallrye.reactive.messaging.ce.CloudEventMetadata;

@ApplicationScoped
public class CloudEventInputDecorator implements InputMessageDecorator {

    @Override
    public <T> T decorate(T event, Metadata metadata) {
        if (event instanceof ProcessDataEvent) {
            metadata.get(CloudEventMetadata.class).map(meta -> addCloudEventInfo(meta, (ProcessDataEvent<?>) event));
        }
        return event;
    }

    private ProcessDataEvent<?> addCloudEventInfo(CloudEventMetadata<?> meta, ProcessDataEvent<?> event) {
        meta.getDataContentType().ifPresent(event::setDataContentType);
        meta.getDataSchema().ifPresent(event::setDataSchema);
        meta.getSubject().ifPresent(event::setSubject);
        meta.getTimeStamp().map(ZonedDateTime::toOffsetDateTime).ifPresent(event::setTime);
        set(meta::getId, event::setId);
        set(meta::getType, event::setType);
        set(meta::getSource, event::setSource);
        set(meta::getSpecVersion, specVersion -> event.setSpecVersion(SpecVersion.valueOf(specVersion)));
        meta.getExtensions().forEach(event::addExtensionAttribute);
        return event;
    }

    private <T> void set(Supplier<T> supplier, Consumer<T> consumer) {
        T value = supplier.get();
        if (value != null) {
            consumer.accept(value);
        }
    }

}
