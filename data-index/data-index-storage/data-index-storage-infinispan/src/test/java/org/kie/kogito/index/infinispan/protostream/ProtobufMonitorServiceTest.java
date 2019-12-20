/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.index.infinispan.protostream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
public class ProtobufMonitorServiceTest {

    @Mock
    ProtobufService protobufService;

    @InjectMocks
    ProtobufMonitorService protobufMonitorService;

    @Test
    public void testLoadingAndUpdatingFiles() throws Exception {
        Path dir = null;
        try {
            dir = Files.createTempDirectory(this.getClass().getName());
            Path sub = Files.createDirectory(dir.resolve("proto"));
            Files.createFile(dir.resolve("kogito-application.proto"));
            Path file1 = Files.createFile(dir.resolve("test1.proto"));
            Path file2 = Files.createFile(sub.resolve("test2.proto"));
            CountDownLatch latch = new CountDownLatch(4);
            doAnswer(args -> {
                latch.countDown();
                return null;
            }).when(protobufService).registerProtoBufferType(any());

            protobufMonitorService.monitor = true;
            protobufMonitorService.protoFiles = Optional.of(dir.toAbsolutePath().toString());
            protobufMonitorService.onFolderWatch = path ->
                    CompletableFuture.runAsync(() -> {
                        try {
                            Files.write(file2, "test".getBytes());
                            Files.write(file1, "test".getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            protobufMonitorService.onStart(null);

            latch.await(1, TimeUnit.MINUTES);
            assertEquals(0, latch.getCount());
        } finally {
            if (dir != null) {
                try {
                    Files.deleteIfExists(dir);
                } catch (IOException e) {
                }
            }
        }
    }

    @Test
    public void testAddingSubFolderAfterStart() throws Exception {
        Path dir = null;
        try {
            dir = Files.createTempDirectory(this.getClass().getName());
            Files.createFile(dir.resolve("kogito-application.proto"));
            Files.createFile(dir.resolve("test1.proto"));
            CountDownLatch latch = new CountDownLatch(2);
            Path proto = dir.resolve("proto");
            doAnswer(args -> {
                latch.countDown();
                return null;
            }).when(protobufService).registerProtoBufferType(any());

            protobufMonitorService.monitor = true;
            protobufMonitorService.protoFiles = Optional.of(dir.toAbsolutePath().toString());
            protobufMonitorService.onFolderWatch = path ->
                    CompletableFuture.runAsync(() -> {
                        try {
                            Path sub = Files.createDirectory(proto);
                            Files.createFile(sub.resolve("test2.txt"));
                            Files.createFile(sub.resolve("test2.proto"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            protobufMonitorService.onStart(null);

            latch.await(1, TimeUnit.MINUTES);
            assertEquals(0, latch.getCount());
        } finally {
            if (dir != null) {
                try {
                    Files.deleteIfExists(dir);
                } catch (IOException e) {
                }
            }
        }
    }
}
