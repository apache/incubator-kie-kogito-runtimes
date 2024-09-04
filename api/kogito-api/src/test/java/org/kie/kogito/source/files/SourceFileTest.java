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
package org.kie.kogito.source.files;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourceFileTests {

    @Test
    void toPosixPath() {
        SourceFile unixTestPetstoreJson = new SourceFile("test/petstore.json");
        SourceFile deepUnixTestPetstoreJson = new SourceFile("foo/bar/test/petstore.json");
        SourceFile windowsTestPetstoreJson = new SourceFile("test\\petstore.json");
        SourceFile deepWindowsTestPetstoreJson = new SourceFile("foo\\bar\\test\\petstore.json");
        SourceFile petstoreJson = new SourceFile("petstore.json");

        assertThat(unixTestPetstoreJson.getUri()).isNotNull();
        assertThat(unixTestPetstoreJson.getUri()).isEqualTo("test/petstore.json");
        assertThat(deepUnixTestPetstoreJson.getUri()).isNotNull();
        assertThat(deepUnixTestPetstoreJson.getUri()).isEqualTo("foo/bar/test/petstore.json");
        assertThat(windowsTestPetstoreJson.getUri()).isNotNull();
        assertThat(windowsTestPetstoreJson.getUri()).isEqualTo("test/petstore.json");
        assertThat(deepWindowsTestPetstoreJson.getUri()).isNotNull();
        assertThat(deepWindowsTestPetstoreJson.getUri()).isEqualTo("foo/bar/test/petstore.json");
        assertThat(petstoreJson.getUri()).isNotNull();
        assertThat(petstoreJson.getUri()).isEqualTo("petstore.json");
    }
}
