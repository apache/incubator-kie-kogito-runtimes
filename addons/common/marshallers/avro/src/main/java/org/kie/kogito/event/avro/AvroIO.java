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
package org.kie.kogito.event.avro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.util.Utf8;
import org.kie.kogito.event.cloudevents.utils.CloudEventUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;

import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;

import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.DATA;

public class AvroIO {

    private static final String ATTRIBUTES = "attribute";
    private static final Utf8 SPEC_VERSION_UTF = new Utf8("specversion");

    private final Schema ceSchema;
    private final AvroMapper avroMapper;

    public AvroIO() throws IOException {
        this.ceSchema = getCloudEventSchema();
        this.avroMapper = getAvroMapper();
    }

    public byte[] writeObject(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        avroMapper.writer(getAvroSchema(obj.getClass())).writeValue(out, obj);
        out.flush();
        return out.toByteArray();
    }

    public <T> T readObject(byte[] payload, Class<T> outputClass, Class<?>... parametrizedClasses) throws IOException {
        final JavaType type = Objects.isNull(parametrizedClasses) ? avroMapper.getTypeFactory().constructType(outputClass)
                : avroMapper.getTypeFactory().constructParametricType(outputClass, parametrizedClasses);
        return avroMapper.readerFor(type)
                .with(getAvroSchema(outputClass))
                .readValue(payload);
    }

    public byte[] writeCloudEvent(CloudEvent event) throws IOException {
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(ceSchema);
        GenericRecordBuilder builder = new GenericRecordBuilder(ceSchema);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(bytes, null);
        Map<String, Object> attrsMap = event.getAttributeNames().stream().filter(k -> event.getAttribute(k) != null).collect(Collectors.toMap(k -> k, k -> fromJavaObject(event.getAttribute(k))));
        // Cloud Event Avro spec https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/formats/avro-format.md  does not have extension, so passing extensions as attributes
        attrsMap.putAll(event.getExtensionNames().stream().collect(Collectors.toMap(k -> k, k -> fromJavaObject(event.getExtension(k)))));
        builder.set(ATTRIBUTES, attrsMap);
        builder.set(DATA, ByteBuffer.wrap(event.getData().toBytes()));
        writer.write(builder.build(), encoder);
        encoder.flush();
        return bytes.toByteArray();
    }

    private Object fromJavaObject(Object value) {
        if (value instanceof Number || value instanceof Boolean || value instanceof String || value instanceof ByteBuffer) {
            return value;
        } else if (value instanceof byte[]) {
            return ByteBuffer.wrap((byte[]) value);
        } else {
            return value.toString();
        }
    }

    public CloudEvent readCloudEvent(byte[] bytes) throws IOException {
        GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(ceSchema);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        GenericRecord record = reader.read(null, decoder);
        Map<Utf8, Object> attrs = (Map<Utf8, Object>) record.get(ATTRIBUTES);
        SpecVersion specVersion = SpecVersion.parse(attrs.remove(SPEC_VERSION_UTF).toString());
        CloudEventBuilder builder = CloudEventBuilder.fromSpecVersion(specVersion);
        specVersion.getAllAttributes().forEach(k -> {
            Object v = attrs.remove(new Utf8(k));
            if (v != null) {
                CloudEventUtils.withAttribute(builder, k, v);
            }
        });
        Object data = record.get(DATA);
        if (data instanceof ByteBuffer) {
            builder.withData(((ByteBuffer) data).array());
        }
        attrs.forEach((k, v) -> CloudEventUtils.withExtension(builder, k.toString(), v));
        return builder.build();
    }

    private AvroSchema getAvroSchema(Class<?> clazz) {
        return new AvroSchema(getSchema(clazz));
    }

    private Schema getSchema(Class<?> clazz) {
        return ReflectData.get().getSchema(clazz);
    }

    private static Schema getCloudEventSchema() throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("spec.avsc")) {
            if (is == null) {
                throw new IOException("cannot load cloud event schema");
            }
            return new Schema.Parser().parse(is);
        }
    }

    private static final AvroMapper getAvroMapper() {
        AvroMapper mapper = new AvroMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
}
