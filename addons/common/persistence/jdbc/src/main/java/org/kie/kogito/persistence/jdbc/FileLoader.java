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
package org.kie.kogito.persistence.jdbc;

import java.io.InputStream;
import java.util.List;

public class FileLoader {

    static List<String> getQueryFromFile(final String dbType, final String scriptName) {
        final String fileName = String.format("sql/%s_%s.sql", scriptName, dbType);
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                throw new IllegalStateException(String.format("Impossible to find %s", fileName));
            }
            byte[] buffer = new byte[stream.available()];
            int count = 0;
            while ((count = stream.read(buffer)) > 0) {
            }
            String[] statements = new String(buffer).split(";");
            return List.of(statements);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error reading query script file %s", fileName), e);
        }
    }

}
