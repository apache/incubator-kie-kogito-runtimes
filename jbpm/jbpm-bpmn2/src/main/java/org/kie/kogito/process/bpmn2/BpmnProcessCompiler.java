package org.kie.kogito.process.bpmn2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.drools.core.xml.SemanticModule;
import org.drools.core.xml.SemanticModules;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.workflow.core.WorkflowProcess;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.kogito.process.ProcessConfig;

public class BpmnProcessCompiler {

    private final SemanticModules bpmnSemanticModules;

    public BpmnProcessCompiler(SemanticModule... modules) {
        this.bpmnSemanticModules = new SemanticModules();

        if (modules.length == 0) {
            // add default
            this.bpmnSemanticModules.addSemanticModule(new BPMNSemanticModule());
            this.bpmnSemanticModules.addSemanticModule(new BPMNExtensionsSemanticModule());
            this.bpmnSemanticModules.addSemanticModule(new BPMNDISemanticModule());
        } else {
            for (SemanticModule module : modules) {
                this.bpmnSemanticModules.addSemanticModule(module);
            }
        }
    }

    protected SemanticModules getSemanticModules() {
        return bpmnSemanticModules;
    }

    public List<BpmnProcess> from(ProcessConfig config, Resource... resources) {
        try {
            List<Process> processes = new ArrayList<>();
            XmlProcessReader xmlReader = new XmlProcessReader(
                                                              getSemanticModules(),
                                                              Thread.currentThread().getContextClassLoader());

            for (Resource resource : resources) {
                processes.addAll(xmlReader.read(resource.getReader()));
            }
            List<BpmnProcess> bpmnProcesses = processes.stream()
                                                       .map(p -> (config == null ? new BpmnProcess(p) : new BpmnProcess(p, config)))
                                                       .collect(Collectors.toList());

            bpmnProcesses.forEach(p -> {

                for (Node node : ((WorkflowProcess) p.legacyProcess()).getNodesRecursively()) {

                    processNode(node, bpmnProcesses);
                }
            });

            return (List<BpmnProcess>) bpmnProcesses;
        } catch (Exception e) {
            throw new BpmnProcessReaderException(e);
        }
    }

    protected void processNode(Node node, List<BpmnProcess> bpmnProcesses) {

    }
}
