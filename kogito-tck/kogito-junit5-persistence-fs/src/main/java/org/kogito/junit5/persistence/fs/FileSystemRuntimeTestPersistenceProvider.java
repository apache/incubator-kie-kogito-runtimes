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

package org.kogito.junit5.persistence.fs;

import java.nio.file.Files;
import java.nio.file.Path;

import org.kie.kogito.junit.deployment.spi.RuntimeTestPersistenceProvider;
import org.kie.kogito.persistence.KogitoProcessInstancesFactory;

public class FileSystemRuntimeTestPersistenceProvider implements RuntimeTestPersistenceProvider<KogitoProcessInstancesFactory> {

    @Override
    public void prepare(KogitoProcessInstancesFactory prepare) {
        try {
            Path buildPath = Files.createTempDirectory("KOGITO_FS_STORAGE_");
            prepare.setPath(buildPath.toString());
        } catch (Exception e) {
            throw new RuntimeException("cannot create kogito fs storage", e);
        }
    }

}
