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

package org.kie.submarine.process.bpmn2;

import java.util.List;
import java.util.stream.Collectors;

import org.drools.core.xml.SemanticModules;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.process.instance.LightProcessRuntimeServiceProvider;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.submarine.process.ProcessInstance;
import org.kie.submarine.process.impl.AbstractProcess;

public class BpmnProcess extends AbstractProcess<BpmnVariables> {

    private static final SemanticModules BPMN_SEMANTIC_MODULES = new SemanticModules();

    static {
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNExtensionsSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNDISemanticModule());
    }

    private final Process process;

    public BpmnProcess(Process p) {
        process = p;
    }

    public ProcessInstance<BpmnVariables> createInstance() {
        return new BpmnProcessInstance(this, BpmnVariables.create(), this.createLegacyProcessRuntime());
    }

    @Override
    public ProcessInstance<BpmnVariables> createInstance(BpmnVariables variables) {
        return new BpmnProcessInstance(this, variables, this.createLegacyProcessRuntime());
    }

    public static List<BpmnProcess> from(Resource resource) {
        try {
            XmlProcessReader xmlReader = new XmlProcessReader(
                    BPMN_SEMANTIC_MODULES,
                    Thread.currentThread().getContextClassLoader());
            List<Process> processes = xmlReader.read(resource.getReader());
            return processes.stream().map(BpmnProcess::new).collect(Collectors.toList());
        } catch (Exception e) {
            throw new BpmnProcessReaderException(e);
        }
    }

    @Override
    protected Process legacyProcess() {
        return process;
    }
}
