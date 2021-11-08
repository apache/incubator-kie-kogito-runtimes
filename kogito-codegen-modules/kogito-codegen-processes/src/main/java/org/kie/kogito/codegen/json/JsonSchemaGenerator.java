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
package org.kie.kogito.codegen.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbpm.util.JsonSchemaUtil;
import org.kie.kogito.ProcessInput;
import org.kie.kogito.UserTask;
import org.kie.kogito.UserTaskParam;
import org.kie.kogito.codegen.VariableInfo;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.GeneratedFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.CustomDefinition.AttributeInclusion;
import com.github.victools.jsonschema.generator.CustomPropertyDefinition;
import com.github.victools.jsonschema.generator.FieldScope;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.generator.impl.DefinitionKey;
import com.github.victools.jsonschema.generator.impl.SchemaGenerationContextImpl;
import com.github.victools.jsonschema.generator.naming.DefaultSchemaDefinitionNamingStrategy;
import com.github.victools.jsonschema.generator.naming.SchemaDefinitionNamingStrategy;

public class JsonSchemaGenerator {

    public static final Logger logger = LoggerFactory.getLogger(JsonSchemaGenerator.class);
    public static final SchemaVersion DEFAULT_SCHEMA_VERSION = SchemaVersion.DRAFT_7;
    private static final GeneratedFileType JSON_SCHEMA_TYPE = GeneratedFileType.of("JSON_SCHEMA", GeneratedFileType.Category.RESOURCE, true, true);

    private final Map<String, DefinitionKey> definitionKeyCache = new HashMap<>();
    private final Map<String, List<Class<?>>> map;
    private final SchemaVersion schemaVersion;

    public static class ClassBuilder {

        private Stream<Class<?>> stream;
        private Function<? super Class<?>, String> getSchemaName = JsonSchemaGenerator::getSchemaName;
        private Predicate<? super Class<?>> shouldGenSchema = JsonSchemaGenerator::shouldGenSchema;
        private SchemaVersion schemaVersion = DEFAULT_SCHEMA_VERSION;

        public ClassBuilder(Stream<Class<?>> stream) {
            this.stream = stream;
        }

        public ClassBuilder withSchemaNameFunction(Function<? super Class<?>, String> getSchemaName) {
            this.getSchemaName = getSchemaName;
            return this;
        }

        public ClassBuilder withGenSchemaPredicate(Predicate<? super Class<?>> shouldGenSchema) {
            this.shouldGenSchema = shouldGenSchema;
            return this;
        }

        public ClassBuilder withSchemaVersion(String schemaVersion) {
            this.schemaVersion = schemaVersion == null ? DEFAULT_SCHEMA_VERSION : SchemaVersion.valueOf(schemaVersion.trim().toUpperCase());
            return this;
        }

        public JsonSchemaGenerator build() {
            Map<String, List<Class<?>>> map = stream
                    .filter(shouldGenSchema)
                    .filter(this::ensureHasAnnotations)
                    .collect(Collectors.groupingBy(getSchemaName));

            return new JsonSchemaGenerator(map, schemaVersion);
        }

        private boolean ensureHasAnnotations(Class<?> c) {
            boolean needsJsonSchema = shouldGenSchema(c);
            if (!needsJsonSchema) {
                logger.warn("Could not retrieve neither UserTask nor Process annotation from class {} but was expected. " +
                        "This may be a class loader bug. If JsonSchemas have been generated you may ignore this message.", c);
            }
            return needsJsonSchema;
        }
    }

    private JsonSchemaGenerator(Map<String, List<Class<?>>> map, SchemaVersion schemaVersion) {
        this.map = map;
        this.schemaVersion = schemaVersion;
    }

    public Collection<GeneratedFile> generate() throws IOException {

        SchemaGeneratorConfigBuilder builder = new SchemaGeneratorConfigBuilder(schemaVersion, OptionPreset.PLAIN_JSON);
        builder.with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
        builder.forTypesInGeneral()
                .withStringFormatResolver(target -> target.getSimpleTypeDescription().equals("Date") ? "date-time" : null);
        builder.forFields()
                .withIgnoreCheck(JsonSchemaGenerator::checkFields)
                .withCustomDefinitionProvider(this::getInputOutput);
        SchemaGenerator generator = new SchemaGenerator(builder.build());
        ObjectWriter writer = new ObjectMapper().writer();

        Collection<GeneratedFile> files = new ArrayList<>();
        for (Map.Entry<String, List<Class<?>>> entry : map.entrySet()) {
            ObjectNode merged = null;
            for (Class<?> c : entry.getValue()) {
                ObjectNode read = generator.generateSchema(c);
                if (merged == null) {
                    merged = read;
                } else {
                    JsonUtils.merge(read, merged);
                }
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                writer.writeValue(outputStream, merged);
                files.add(new GeneratedFile(JSON_SCHEMA_TYPE, pathFor(entry.getKey()), outputStream.toByteArray()));
            }
        }
        return files;
    }

    private CustomPropertyDefinition getInputOutput(FieldScope scope, SchemaGenerationContext context) {
        UserTaskParam param = scope.getAnnotation(UserTaskParam.class);

        if (param != null) {
            ObjectNode objectNode = context.createStandardDefinitionReference(scope.getDeclaredType(), null);
            objectNode.put(param.value().toString().toLowerCase(), true);

            Optional<DefinitionKey> optional = lookupDefinitionKey(scope.getDeclaredType(), context);

            if (optional.isPresent()) {
                DefinitionKey definitionKey = optional.get();
                SchemaDefinitionNamingStrategy strategy = Optional.ofNullable(context.getGeneratorConfig().getDefinitionNamingStrategy())
                        .orElse(new DefaultSchemaDefinitionNamingStrategy());
                String refPath =
                        context.getKeyword(SchemaKeyword.TAG_REF_MAIN) + "/" + context.getKeyword(SchemaKeyword.TAG_DEFINITIONS) + "/" + strategy.getDefinitionNameForKey(definitionKey, context);
                objectNode.put(context.getKeyword(SchemaKeyword.TAG_REF), refPath);
            }

            return new CustomPropertyDefinition(objectNode, AttributeInclusion.YES);
        }
        return null;
    }

    private Optional<DefinitionKey> lookupDefinitionKey(ResolvedType type, SchemaGenerationContext context) {
        DefinitionKey definitionKey = this.definitionKeyCache.computeIfAbsent(type.getTypeName(), typeName -> {
            return ((SchemaGenerationContextImpl) context).getDefinedTypes().stream()
                    .filter(key -> key.getType().equals(type))
                    .findFirst()
                    .orElse(null);
        });
        return Optional.ofNullable(definitionKey);
    }

    private static String getSchemaName(Class<?> c) {
        if (c.isAnnotationPresent(ProcessInput.class)) {
            ProcessInput process = c.getAnnotation(ProcessInput.class);
            return JsonSchemaUtil.getJsonSchemaName(process.processName());
        }
        UserTask userTask = c.getAnnotation(UserTask.class);
        return JsonSchemaUtil.getJsonSchemaName(userTask.processName(), userTask.taskName());
    }

    private static boolean shouldGenSchema(Class<?> c) {
        return c.isAnnotationPresent(UserTask.class) || c.isAnnotationPresent(ProcessInput.class);
    }

    private static boolean checkFields(FieldScope fieldScope) {
        return (fieldScope.getDeclaringType().getErasedType().isAnnotationPresent(UserTask.class) && fieldScope.getAnnotation(UserTaskParam.class) == null)
                || (fieldScope.getDeclaringType().getErasedType().isAnnotationPresent(ProcessInput.class) && fieldScope.getAnnotation(VariableInfo.class) == null);
    }

    private Path pathFor(String name) {
        return JsonSchemaUtil.getJsonDir().resolve(JsonSchemaUtil.getFileName(name));
    }
}
