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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PathUtilsTest {

    private Path base;

    @BeforeEach
    void setUp() {
        base = Paths.get("src/test/resources/META-INF/processSVG").toAbsolutePath();
    }

    @Test
    void testResolveSecureAcceptsValidPath() {
        Path result = PathUtils.resolveSecure(base, "travels.svg");
        assertThat(result.startsWith(base)).isTrue();
    }

    @Test
    void testResolveSecureRejectsTraversal() {
        assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(() -> PathUtils.resolveSecure(base, "../outside.svg"))
                .withMessageContaining("Attempted path traversal:"); // Adjusted to match actual message
    }
}
