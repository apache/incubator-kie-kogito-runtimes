/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.process.persistence.proto;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.GeneratedFileType;

public abstract class AbstractProtoGenerator<T> implements ProtoGenerator {

    private static final String GENERATED_PROTO_RES_PATH = "META-INF/resources/persistence/protobuf/";
    private static final String LISTING_FILE = "list.json";

    protected ObjectMapper mapper;
    protected Collection<T> dataClasses;
    protected T persistenceClass;

    protected AbstractProtoGenerator(T persistenceClass, Collection<T> dataClasses) {
        this.dataClasses = dataClasses;
        this.persistenceClass = persistenceClass;
        mapper = new ObjectMapper();
    }

    @Override
    public Collection<GeneratedFile> generateDataClasses() {
        List<GeneratedFile> generatedFiles = new ArrayList<>();

        dataClasses.stream()
                .map(this::generateModelClassProto)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(generatedFiles::add);

        try {
            this.generateProtoListingFile(generatedFiles).ifPresent(generatedFiles::add);
        } catch (IOException e) {
            throw new UncheckedIOException("Error during proto listing file creation", e);
        }

        return generatedFiles;
    }

    /**
     * Generates the proto files from the given model.
     */
    public final GeneratedFile generateProtoFiles(final String processId, final Proto modelProto) {
        String protoFileName = processId + ".proto";
        return new GeneratedFile(GeneratedFileType.RESOURCE,
                                 GENERATED_PROTO_RES_PATH + protoFileName,
                                 modelProto.toString());
    }

    /**
     * Iterates over the generated files and extract all the proto files. Then it creates and add to the generated files collection
     * a listing file ({@link #LISTING_FILE}) from its content.
     *
     * @param generatedFiles  The list of generated files.
     * @throws IOException if something wrong occurs during I/O
     */
    public final Optional<GeneratedFile> generateProtoListingFile(Collection<GeneratedFile> generatedFiles) throws IOException {
        List<String> fileNames = generatedFiles.stream()
                .filter(x -> x.relativePath().contains(GENERATED_PROTO_RES_PATH))
                .map(x -> x.relativePath().substring(x.relativePath().lastIndexOf("/") + 1))
                .collect(Collectors.toList());

        if (!fileNames.isEmpty()) {
            return Optional.of(new GeneratedFile(GeneratedFileType.RESOURCE,
                                                 GENERATED_PROTO_RES_PATH + LISTING_FILE,
                                                 mapper.writeValueAsString(fileNames)));
        }
        return Optional.empty();
    }

    protected abstract Proto generate(String messageComment, String fieldComment, String packageName, T dataModel, String... headers);

    protected abstract Optional<GeneratedFile> generateModelClassProto(T modelClazz);

    protected static abstract class AbstractProtoGeneratorBuilder<E, T extends ProtoGenerator> implements Builder<E, T> {
        protected E persistenceClass;

        protected abstract Collection<E> extractDataClasses(Collection<E> modelClasses);

        protected abstract boolean isValidModelClass(E modelClass);

        @Override
        public Builder<E, T> withPersistenceClass(E persistenceClass) {
            this.persistenceClass = persistenceClass;
            return this;
        }

        @Override
        public T buildWithModelClasses(Collection<E> modelClasses) {
            if (modelClasses == null) {
                return buildWithDataClasses(null);
            }
            Collection<String> invalidClasses = modelClasses.stream()
                    .filter(mc -> !isValidModelClass(mc))
                    .map(Objects::toString)
                    .collect(Collectors.toList());
            if(!invalidClasses.isEmpty()) {
                throw new IllegalArgumentException("Found classes that are not models: " + String.join(",", invalidClasses));
            }
            return buildWithDataClasses(extractDataClasses(modelClasses));
        }
    }
}
