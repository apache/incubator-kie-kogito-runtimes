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
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
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

import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.CONTENT_TYPE;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.DATA;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.DATA_SCHEMA;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.ID;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.SOURCE;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.SPEC_VERSION;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.SUBJECT;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.TIME;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.TYPE;

public class AvroUtils {

    private static final String ATTRIBUTES = "attribute";
    private static final Utf8 SPEC_VERSION_UTF = new Utf8(SPEC_VERSION);
    private static final Utf8 TYPE_UTF = new Utf8(TYPE);
    private static final Utf8 SOURCE_UTF = new Utf8(SOURCE);
    private static final Utf8 ID_UTF = new Utf8(ID);
    private static final Utf8 CONTENT_TYPE_UTF = new Utf8(CONTENT_TYPE);
    private static final Utf8 DATA_SCHEMA_UTF = new Utf8(DATA_SCHEMA);
    private static final Utf8 TIME_UTF = new Utf8(TIME);
    private static final Utf8 SUBJECT_UTF = new Utf8(SUBJECT);

    private final Schema ceSchema;
    private final AvroMapper avroMapper;

    public AvroUtils() throws IOException {
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
        CloudEventBuilder builder = CloudEventBuilder.fromSpecVersion(SpecVersion.parse(attrs.remove(SPEC_VERSION_UTF).toString()))
                .withType(attrs.remove(TYPE_UTF).toString())
                .withSource(URI.create(attrs.remove(SOURCE_UTF).toString()))
                .withId(attrs.remove(ID_UTF).toString());
        Object value = attrs.remove(CONTENT_TYPE_UTF);
        if (value != null) {
            builder.withDataContentType(value.toString());
        }
        value = attrs.remove(DATA_SCHEMA_UTF);
        if (value != null) {
            builder.withDataSchema(URI.create(value.toString()));
        }
        value = attrs.remove(TIME_UTF);
        if (value != null) {
            builder.withTime(OffsetDateTime.parse(value.toString()));
        }
        value = attrs.get(SUBJECT_UTF);
        if (value != null) {
            builder.withSubject(value.toString());
        }
        value = record.get(DATA);
        if (value instanceof ByteBuffer) {
            builder.withData(((ByteBuffer) value).array());
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
