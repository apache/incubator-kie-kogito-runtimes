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
package org.kie.kogito.addon.source.files;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class SourceFilesResourceTest {

    @Inject
    SourceFilesResource sourceFilesResource;

    @Inject
    SourceFilesProviderImpl sourceFilesProvider;

    @BeforeEach
    void clearSourceFilesProvider() {
        sourceFilesProvider.clear();
    }

    @Test
    void getSourceFiles() {
        sourceFilesProvider.addSourceFile("a_process", new SourceFile("petstore.json"));
        sourceFilesProvider.addSourceFile("a_process", new SourceFile("petstore.sw.json"));
        sourceFilesProvider.addSourceFile("ymlgreet", new SourceFile("ymlgreet.sw.json"));

        Map<String, Collection<SourceFile>> sourceFiles = sourceFilesResource.getSourceFiles();

        assertThat(sourceFiles)
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "a_process", List.of(new SourceFile("petstore.json"), new SourceFile("petstore.sw.json")),
                        "ymlgreet", List.of(new SourceFile("ymlgreet.sw.json"))));
    }

    @Test
    void getSourceFilesByProcessId() {
        sourceFilesProvider.addSourceFile("a_process", new SourceFile("petstore.json"));
        sourceFilesProvider.addSourceFile("a_process", new SourceFile("petstore.sw.json"));

        Collection<SourceFile> sourceFiles = sourceFilesResource.getSourceFiles("a_process");

        assertThat(sourceFiles)
                .containsExactlyInAnyOrder(new SourceFile("petstore.json"), new SourceFile("petstore.sw.json"));
    }

    @Test
    void getSourceFile() throws IOException {
        sourceFilesProvider.addSourceFile("a_process", new SourceFile("petstore.json"));

        Response response = sourceFilesResource.getSourceFile("petstore.json");

        assertThat(response.getStatus())
                .isEqualTo(200);

        assertThat(response.getHeaderString("Content-Disposition"))
                .isEqualTo("inline; filename=\"petstore.json\"");

        assertThat(response.getHeaderString("Content-Length"))
                .isEqualTo("5189");
    }

    @Test
    void getSourceFileThatNotExistsShouldReturn404() throws IOException {
        sourceFilesProvider.addSourceFile("a_process", new SourceFile("file_that_not_exists.json"));

        Response response = sourceFilesResource.getSourceFile("file_that_not_exists.json");

        assertThat(response.getStatus())
                .isEqualTo(404);
    }

    @Test
    void getSourceFileThatIsNotInSourceFilesProviderShouldReturn404() throws IOException {
        Response response = sourceFilesResource.getSourceFile("petstore.json");

        assertThat(response.getStatus())
                .isEqualTo(404);
    }
}
