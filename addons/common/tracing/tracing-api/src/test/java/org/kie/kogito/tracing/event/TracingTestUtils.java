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
package org.kie.kogito.tracing.event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TracingTestUtils {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T readResource(String name, Class<T> clazz) {
        try {
            return MAPPER.readValue(TracingTestUtils.class.getResource(name), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Can't read test resource " + name, e);
        }
    }

    public static String readResourceAsString(String name) {
        try {
            return readFromInputStream(TracingTestUtils.class.getResourceAsStream(name));
        } catch (Exception e) {
            throw new RuntimeException("Can't read test resource " + name, e);
        }
    }

    public static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    private TracingTestUtils() {
    }
}
