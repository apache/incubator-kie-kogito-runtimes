/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.maven.plugin.process;

import org.drools.core.util.StringUtils;
import org.jbpm.compiler.canonical.ModelMetaData;
import org.jbpm.compiler.canonical.ProcessToExecModelGenerator;
import org.kie.api.definition.process.WorkflowProcess;

public class ModelClassGenerator {

    private final WorkflowProcess workFlowProcess;
    private String modelFileName;
    private ModelMetaData modelMetaData;
    private String modelClassName;

    public ModelClassGenerator(WorkflowProcess workFlowProcess) {
        String pid = workFlowProcess.getId();
        String name = ProcessToExecModelGenerator.extractProcessId(pid);
        this.modelClassName = workFlowProcess.getPackageName() + "." +
                StringUtils.capitalize(name) + "Model";

        this.workFlowProcess = workFlowProcess;
    }

    public ModelMetaData generate() {
        // create model class for all variables
        modelMetaData = ProcessToExecModelGenerator.INSTANCE.generateModel(workFlowProcess);
        modelFileName = modelMetaData.getModelClassName().replace('.', '/') + ".java";
        return modelMetaData;
    }

    public String generatedFilePath() {
        return modelFileName;
    }

    public String simpleName() {
        return modelMetaData.getModelClassSimpleName();
    }

    public String className() {
        return modelClassName;
    }
}
