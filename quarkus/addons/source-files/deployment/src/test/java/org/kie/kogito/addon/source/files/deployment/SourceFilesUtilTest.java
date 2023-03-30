/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addon.source.files.deployment;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import static org.assertj.core.api.Assertions.assertThat;

class SourceFilesUtilTest {

    private static final FileSystem inMemoryFileSystem = Jimfs.newFileSystem(Configuration.unix());

    @AfterAll
    static void tearDown() throws IOException {
        inMemoryFileSystem.close();
    }

    @Test
    void test() throws IOException {
        Path resources = createResources();
        List<String> sourceFiles = SourceFilesUtil.getSourceFiles(List.of(resources));

        assertThat(sourceFiles)
                .containsExactlyInAnyOrder(
                        "org/kie/kogito/approval.bpmn",
                        "org/kie/kogito/jsongreet.sw.json",
                        "org/kie/kogito/orderItems.bpmn2",
                        "petstore_root.sw.json");
    }

    private static Path createResources() throws IOException {
        Path path = Files.createDirectory(inMemoryFileSystem.getPath("/path"));
        Path to = Files.createDirectory(path.resolve("to"));
        Path my = Files.createDirectory(to.resolve("my"));
        Path resources = Files.createDirectory(my.resolve("resources"));
        Files.createFile(resources.resolve("application.properties"));
        Path org = Files.createDirectory(resources.resolve("org"));
        Path kie = Files.createDirectory(org.resolve("kie"));
        Path kogito = Files.createDirectory(kie.resolve("kogito"));
        Files.createFile(kogito.resolve("PersonValidation.drl"));
        Files.createFile(kogito.resolve("approval.bpmn"));
        Files.createFile(kogito.resolve("jsongreet.sw.json"));
        Files.createFile(kogito.resolve("orderItems.bpmn2"));
        Files.createFile(kogito.resolve("something.data"));
        Files.createFile(resources.resolve("any.json"));
        Files.createFile(resources.resolve("petstore_root.sw.json"));
        return resources;
    }
}
