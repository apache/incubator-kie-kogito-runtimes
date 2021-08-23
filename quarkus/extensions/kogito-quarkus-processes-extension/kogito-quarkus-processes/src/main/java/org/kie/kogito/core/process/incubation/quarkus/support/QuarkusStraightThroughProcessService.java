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

package org.kie.kogito.core.process.incubation.quarkus.support;

import java.nio.file.Path;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.kogito.Model;
import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.Id;
import org.kie.kogito.incubation.common.MapDataContext;
import org.kie.kogito.incubation.processes.ProcessId;
import org.kie.kogito.incubation.processes.services.StraightThroughProcessService;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;

@ApplicationScoped
public class QuarkusStraightThroughProcessService implements StraightThroughProcessService {
    Processes processes;

    @Inject
    public QuarkusStraightThroughProcessService(Processes processes) {
        this.processes = processes;
    }

    @Override
    public DataContext evaluate(Id processId, DataContext inputContext) {
        Path processPath = processId.toLocalId().asPath();
        if (processPath.getName(0).toString().equals(ProcessId.PREFIX)) {
            Process<? extends Model> process = processes.processById(processPath.getName(1).toString());
            Model model = process.createModel();
            MapDataContext mdc = inputContext.as(MapDataContext.class);
            model.fromMap(mdc.toMap());
            ProcessInstance<? extends Model> instance = process.createInstance(model);
            instance.start();
            Map<String, Object> map = instance.variables().toMap();
            return MapDataContext.of(map);
        } else {
            throw new IllegalArgumentException("Not a valid processId " + processPath);
        }

    }
}
