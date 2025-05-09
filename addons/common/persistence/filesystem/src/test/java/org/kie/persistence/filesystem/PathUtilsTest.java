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
package org.kie.persistence.filesystem;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.kie.kogito.persistence.filesystem.PathUtils;

import static org.junit.jupiter.api.Assertions.*;

class PathUtilsTest {

    @Test
    void testResolveSecure_ThrowsSecurityException_WhenPathDoesNotStartWithBase() {
        Path base = Paths.get("/home/user");
        String userProvided = "../otherfolder/file.txt";

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathUtils.resolveSecure(base, userProvided);
        });

        assertEquals("Attempted path traversal: ../otherfolder/file.txt", exception.getMessage());
    }

    @Test
    void testResolveSecure_ReturnsResolvedPath_WhenPathStartsWithBase() {
        Path base = Paths.get("/home/user");
        String userProvided = "documents/myfile.txt";

        Path resolvedPath = PathUtils.resolveSecure(base, userProvided);

        Path expectedPath = base.resolve(userProvided).normalize();
        assertEquals(expectedPath, resolvedPath);
    }

    @Test
    void testResolveSecureRejectsPathTraversal() {
        Path base = Paths.get("/home/user");
        String userProvided = "../hack";

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathUtils.resolveSecure(base, userProvided);
        });

        assertEquals("Attempted path traversal: ../hack", exception.getMessage());
    }

    @Test
    void testResolveSecureRejectsMultiplePathTraversals() {
        Path base = Paths.get("/home/user");
        String userProvided = "../../etc/passwd"; // Trying to go back two levels

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathUtils.resolveSecure(base, userProvided);
        });

        assertEquals("Attempted path traversal: ../../etc/passwd", exception.getMessage());
    }

}
