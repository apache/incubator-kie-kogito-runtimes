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

import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.CONTENT_TYPE;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.DATA;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.DATA_SCHEMA;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.ID;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.SOURCE;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.SPEC_VERSION;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.SUBJECT;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.TIME;
import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.TYPE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.kie.kogito.event.cloudevents.utils.CloudEventUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;

import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;

public class AvroUtils {

    static final String EXTENSIONS = "extensions";
    static final String SCHEMA_NAME = "CloudEvent";

    private final AvroMapper avroMapper;
    private final Schema ceSchema;

    public AvroUtils() {
        this(getAvroMapper());
    }

    public AvroUtils(AvroMapper mapper) {
        this.avroMapper = mapper;
        this.ceSchema = getCloudEventSchema();
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
        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>(ceSchema);
        GenericRecordBuilder builder = new GenericRecordBuilder(ceSchema);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(bytes, null);
        builder.set(SPEC_VERSION, event.getSpecVersion().toString());
        if (event.getData() != null) {
            builder.set(DATA, ByteBuffer.wrap(event.getData().toBytes()));
        }
        if (event.getDataSchema() != null) {
            builder.set(DATA_SCHEMA, event.getDataSchema().toString());
        }
        builder.set(ID, event.getId());
        builder.set(SOURCE, event.getSource().toString());
        if (event.getSubject() != null) {
            builder.set(SUBJECT, event.getSubject());
        }
        if (event.getDataContentType() != null) {
            builder.set(CONTENT_TYPE, event.getDataContentType());
        }
        if (event.getTime() != null) {
            builder.set(TIME, event.getTime().toString());
        }
        builder.set(TYPE, event.getType());
        builder.set(AvroUtils.EXTENSIONS, event.getExtensionNames().stream().collect(Collectors.toMap(k -> k, k -> event.getExtension(k))));
        writer.write(builder.build(), encoder);
        encoder.flush();
        return bytes.toByteArray();
    }

    public CloudEvent readCloudEvent(byte[] bytes) throws IOException {
        GenericDatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(ceSchema);
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
        GenericRecord record = reader.read(null, decoder);
        CloudEventBuilder builder = CloudEventBuilder.fromSpecVersion(SpecVersion.parse(record.get(SPEC_VERSION).toString()))
                .withType(record.get(TYPE).toString())
                .withSource(URI.create(record.get(SOURCE).toString()))
                .withId(record.get(ID).toString());
        Object value = record.get(CONTENT_TYPE);
        if (value != null) {
            builder.withDataContentType(value.toString());
        }
        value = record.get(DATA_SCHEMA);
        if (value != null) {
            builder.withDataSchema(URI.create(value.toString()));
        }
        value = record.get(TIME);
        if (value != null) {
            builder.withTime(OffsetDateTime.parse(value.toString()));
        }
        value = record.get(SUBJECT);
        if (value != null) {
            builder.withSubject(value.toString());
        }
        value = record.get(DATA);
        if (value instanceof ByteBuffer) {
            builder.withData(((ByteBuffer) value).array());
        }
        ((Map<Object, Object>) record.get(EXTENSIONS)).forEach((k, v) -> CloudEventUtils.withExtension(builder, k.toString(), v));
        return builder.build();
    }

    private AvroSchema getAvroSchema(Class<?> clazz) {
        return new AvroSchema(getSchema(clazz));
    }

    private Schema getSchema(Class<?> clazz) {
        return ReflectData.get().getSchema(clazz);
    }

    private static final AvroMapper getAvroMapper() {
        AvroMapper mapper = new AvroMapper();
        mapper.registerModule(JsonFormat.getCloudEventJacksonModule()).findAndRegisterModules();
        return mapper;
    }

    private static Schema nullableString() {
        return nullableType(Schema.Type.STRING);
    }

    private static Schema nullableType(Schema.Type type) {
        return Schema.createUnion(Schema.create(Schema.Type.NULL), Schema.create(type));
    }

    private static Schema getCloudEventSchema() {
        return SchemaBuilder.builder().record(SCHEMA_NAME).fields()
                .name(SPEC_VERSION).type(Schema.create(Schema.Type.STRING)).noDefault()
                .name(DATA).type(nullableType(Type.BYTES)).withDefault(null)
                .name(ID).type(Schema.create(Schema.Type.STRING)).noDefault()
                .name(DATA_SCHEMA).type(nullableString()).withDefault(null)
                .name(SOURCE).type(Schema.create(Schema.Type.STRING)).noDefault()
                .name(SUBJECT).type(nullableString()).withDefault(null)
                .name(CONTENT_TYPE).type(nullableString()).withDefault(null)
                .name(TIME).type(nullableString()).withDefault(null)
                .name(TYPE).type(Schema.create(Schema.Type.STRING)).noDefault()
                .name(EXTENSIONS).type(Schema.createMap(Schema.createUnion(
                        Schema.create(Type.STRING),
                        Schema.create(Type.BOOLEAN),
                        Schema.create(Type.INT),
                        Schema.create(Type.DOUBLE),
                        Schema.create(Type.FLOAT),
                        Schema.create(Type.LONG))))
                .noDefault()
                .endRecord();
    }
}
