/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.svg;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.svg.dataindex.DataIndexClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class AbstractProcessSvgServiceTest {
    private TestProcessSvgService service;

    @BeforeEach
    void setUp() {
        Path securePath = Paths.get("src/test/resources/META-INF/processSVG").toAbsolutePath();
        service = new TestProcessSvgService(null, Optional.of(securePath.toString()),
                "#C0C0C0", "#030303", "#FF0000");
    }

    @Test
    void testResolveSecureAcceptsValidPath() {
        Path base = Paths.get("src/test/resources/META-INF/processSVG").toAbsolutePath();
        Path result = service.callResolveSecure(base, "travels.svg");
        assertThat(result.startsWith(base)).isTrue();
    }

    @Test
    void testResolveSecureRejectsTraversal() {
        Path base = Paths.get("src/test/resources/META-INF/processSVG").toAbsolutePath();
        assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(() -> service.callResolveSecure(base, "../outside.svg"))
                .withMessageContaining("Attempted path traversal");
    }

    @Test
    void testGetProcessSvgFromValidPath() {
        Optional<String> svg = service.getProcessSvg("travels");
        assertThat(svg).isPresent();
        assertThat(svg.get()).contains("<svg");
    }

    @Test
    void testGetProcessSvgFromInvalidPath() {
        Optional<String> svg = service.getProcessSvg("nonexistent");
        assertThat(svg).isEmpty();
    }

    /**
     * Helper subclass to expose protected resolveSecure() method
     */
    static class TestProcessSvgService extends AbstractProcessSvgService {
        public TestProcessSvgService(DataIndexClient client, Optional<String> path, String cc, String cbc, String abc) {
            super(client, path, cc, cbc, abc);
        }

        public Path callResolveSecure(Path baseDir, String fileName) {
            return super.resolveSecure(baseDir, fileName);
        }
    }
}
