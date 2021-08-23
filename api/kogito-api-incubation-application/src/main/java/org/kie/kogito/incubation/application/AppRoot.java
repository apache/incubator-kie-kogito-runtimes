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
package org.kie.kogito.incubation.application;

import java.nio.file.Paths;

import org.kie.kogito.incubation.common.Id;
import org.kie.kogito.incubation.common.PathLocalId;

/**
 * Root path of an application
 */
public abstract class AppRoot extends PathLocalId implements Id {

    private final String name;

    protected AppRoot(String name) {
        super(Paths.get("/"));
        this.name = name;
    }

    /**
     * subclasses should override this using the appropriate
     * DI/ServiceLoading mechanism to allow the pattern <code>appRoot.get(Components.class)...</code>;
     * e.g. <code>appRoot.get(ProcessIds.class).get("my.process.id).tasks().get("my.task")</code>
     */
    abstract public <T extends ComponentRoot> T get(Class<T> providerId);

    /**
     * Name is only used to differentiate multiple applications.
     * Mostly useful in a distributed context, with RemoteIds.
     */
    public String name() {
        return name;
    }

}
