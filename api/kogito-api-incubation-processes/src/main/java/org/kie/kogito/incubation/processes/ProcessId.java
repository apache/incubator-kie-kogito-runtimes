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
package org.kie.kogito.incubation.processes;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.kie.kogito.incubation.common.ComponentRoot;
import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.common.PathLocalId;

public class ProcessId extends PathLocalId implements LocalId {
    public static class Factory implements ComponentRoot {
        public ProcessId get(String processId) {
            return new ProcessId(processId);
        }
    }

    public static final String PREFIX = "processes";

    private final String processId;

    public ProcessId(String processId) {
        super(makePath(processId));
        this.processId = processId;
    }

    public String processId() {
        return processId;
    }

    public ProcessInstanceId.Factory instances() {
        return new ProcessInstanceId.Factory(this);
    }

    @Override
    public LocalId toLocalId() {
        return this;
    }

    private static Path makePath(String processId) {
        try {
            String encodedName = URLEncoder.encode(processId, StandardCharsets.UTF_8.name());
            return Paths.get("/", PREFIX, encodedName);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // fixme create a runtime exception
        }
    }

}
