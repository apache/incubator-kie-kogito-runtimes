/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.serverless.workflow.executor;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jbpm.workflow.core.impl.WorkflowProcessImpl;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.kogito.Addons;
import org.kie.kogito.KogitoEngine;
import org.kie.kogito.Model;
import org.kie.kogito.StaticApplication;
import org.kie.kogito.StaticConfig;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.config.StaticConfigBean;
import org.kie.kogito.event.impl.EventFactoryUtils;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstancesFactory;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.impl.CachedWorkItemHandlerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;
import org.kie.kogito.serverless.workflow.SWFConstants;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;
import org.kie.kogito.serverless.workflow.utils.ConfigResolverHolder;
import org.kie.kogito.serverless.workflow.utils.MultiSourceConfigResolver;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.Workflow;

/**
 * This is the entry point for executing a workflow from a JVM
 * Given a <code>Workflow</code> object, you can execute it by writing
 * <code>
 *  // Generated a flow definition or read it from a file
 *  Workflow flow = ....;
 *  try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
 *       // set input model for flow
 *       Map<String,Object> params = ...;
 *       JsonNodeModel model = application.execute(flow, params);
 *       // do something with returned data;
 * }
 * </code>
 * 
 *
 */
public class StaticWorkflowApplication extends StaticApplication implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(StaticWorkflowApplication.class);
    private final StaticWorkflowProcesses processes = new StaticWorkflowProcesses();
    private final Collection<KogitoWorkItemHandler> handlers = new ArrayList<>();
    private Iterable<StaticApplicationRegister> applicationRegisters;
    private Iterable<StaticWorkflowRegister> workflowRegisters;
    private Iterable<StaticProcessRegister> processRegisters;
    private final Collection<AutoCloseable> closeables = new ArrayList<>();
    private final Map<String, SynchronousQueue<JsonNodeModel>> queues;
    private ProcessInstancesFactory processInstancesFactory;

    private static class StaticCompletionEventListener extends DefaultKogitoProcessEventListener {

        private final Map<String, SynchronousQueue<JsonNodeModel>> queues;

        public StaticCompletionEventListener(Map<String, SynchronousQueue<JsonNodeModel>> queues) {
            this.queues = queues;
        }

        @Override
        public void afterProcessCompleted(ProcessCompletedEvent event) {
            WorkflowProcessInstance instance = (WorkflowProcessInstance) event.getProcessInstance();
            SynchronousQueue<JsonNodeModel> queue = queues.remove(instance.getId());
            if (queue != null) {
                try {
                    if (queue.offer(new JsonNodeModel(instance.getId(), instance.getVariables().get(SWFConstants.DEFAULT_WORKFLOW_VAR)), 1L, TimeUnit.SECONDS)) {
                        logger.debug("waiting process instance {} has been notified about its completion", instance.getId());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static StaticWorkflowApplication create() {
        Properties properties = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException io) {
            logger.warn("Error loading application.properties from classpath", io);
        }
        return create((Map) properties);
    }

    public static StaticWorkflowApplication create(Map<String, Object> properties) {
        Map<String, SynchronousQueue<JsonNodeModel>> queues = new ConcurrentHashMap<>();
        StaticWorkflowApplication application = new StaticWorkflowApplication(properties, queues);
        application.applicationRegisters.forEach(register -> register.register(application));
        return application;
    }

    private StaticWorkflowApplication(Map<String, Object> properties, Map<String, SynchronousQueue<JsonNodeModel>> queues) {
        super(new StaticConfig(new Addons(Collections.emptySet()), new StaticProcessConfig(new CachedWorkItemHandlerConfig(),
                new DefaultProcessEventListenerConfig(new StaticCompletionEventListener(queues)),
                new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory())), new StaticConfigBean()));
        if (!properties.isEmpty()) {
            ConfigResolverHolder.setConfigResolver(MultiSourceConfigResolver.withSystemProperties(properties));
        }
        this.queues = queues;
        applicationRegisters = ServiceLoader.load(StaticApplicationRegister.class);
        workflowRegisters = ServiceLoader.load(StaticWorkflowRegister.class);
        processRegisters = ServiceLoader.load(StaticProcessRegister.class);
    }

    /**
     * Given a workflow, executes it. This is a shortcut for <code>
     * execute(process(flow),data);
     * </code>. It is expected to be used only when you want to execute your flow once.
     * 
     * @param workflow Serverless workflow definition
     * @param data A map containing workflow input parameters
     * @return
     */
    public JsonNodeModel execute(Workflow workflow, Map<String, Object> data) {
        return execute(findOrCreate(workflow), data);
    }

    /**
     * Given a workflow, executes it. This is a shortcut for <code>
     * 	execute(process(flow),data);
     * </code>. It is expected to be used only when you want to execute your flow once.
     * 
     * @param workflow Serverless workflow definition
     * @param data A json containing workflow input parameters
     * @return
     */
    public JsonNodeModel execute(Workflow workflow, JsonNode data) {
        return execute(findOrCreate(workflow), data);
    }

    public StaticWorkflowApplication processInstancesFactory(ProcessInstancesFactory processInstanceFactory) {
        this.processInstancesFactory = processInstanceFactory;
        return this;
    }

    private Process<JsonNodeModel> findOrCreate(Workflow workflow) {
        return findProcessById(workflow.getId()).orElseGet(() -> process(workflow));
    }

    /**
     * Given a process definition, executes it. A process definition can be obtained from the flow by using <code>process</code> method
     * 
     * @param workflow Serverless Workflow definition
     * @param data A map containing workflow input parameters.
     * @return
     */
    public JsonNodeModel execute(Process<JsonNodeModel> process, Map<String, Object> data) {
        return execute(process, new JsonNodeModel(data));
    }

    /**
     * Given a process definition, executes it. A process definition can be obtained from the flow by using <code>process</code> method
     * 
     * @param workflow Serverless Workflow definition
     * @param data A JsonNode containing workflow input parameters.
     * @return
     */
    public JsonNodeModel execute(Process<JsonNodeModel> process, JsonNode data) {
        return execute(process, new JsonNodeModel(data));
    }

    /**
     * Given a process definition, executes it. A process definition can be obtained from a flow by using <code>process</code> method
     * 
     * @param workflow Serverless Workflow definition
     * @param model JsnoNodeModel obtained from a previous execution of another flow
     * @return
     */
    public JsonNodeModel execute(Process<JsonNodeModel> process, JsonNodeModel model) {
        ProcessInstance<JsonNodeModel> processInstance = process.createInstance(model);
        processInstance.start();
        return processInstance.variables();
    }

    /**
     * Parses the flow, generating a process definition. You can reuse that process definition to invoke
     * the same flow several times, using <code>execute</code> method
     * 
     * @param workflow Serverless Worflow definition
     * @return Executable process definition
     */
    public Process<JsonNodeModel> process(Workflow workflow) {
        Process<JsonNodeModel> process = createProcess(workflow);
        processes.map.put(workflow.getId(), process);
        return process;
    }

    public void registerHandler(KogitoWorkItemHandler handler) {
        handlers.add(handler);
    }

    public void registerCloseable(AutoCloseable closeable) {
        closeables.add(closeable);
    }

    public Optional<Process<JsonNodeModel>> findProcessById(String id) {
        return Optional.ofNullable((Process<JsonNodeModel>) processes.processById(id));
    }

    private Optional<ProcessInstance<JsonNodeModel>> findProcessInstance(String id) {
        for (Process<JsonNodeModel> process : processes.map.values()) {
            Optional<ProcessInstance<JsonNodeModel>> pi = process.instances().findById(id);
            if (pi.isPresent()) {
                return pi;
            }
        }
        return Optional.empty();
    }

    public Optional<JsonNodeModel> variables(String id) {
        return findProcessInstance(id).map(ProcessInstance::variables);
    }

    public Optional<JsonNodeModel> waitForFinish(String id, Duration duration) throws InterruptedException, TimeoutException {
        JsonNodeModel model = queues.computeIfAbsent(id, k -> new SynchronousQueue<>()).poll(duration.toMillis(), TimeUnit.MILLISECONDS);
        if (model == null) {
            Optional<ProcessInstance<JsonNodeModel>> pi = findProcessInstance(id);
            if (pi.isEmpty()) {
                queues.remove(id);
                return pi.map(ProcessInstance::variables);
            }
            throw new TimeoutException("Process " + id + " has not finished after " + duration);
        }
        return Optional.of(model);
    }

    private Process<JsonNodeModel> createProcess(Workflow workflow) {
        workflowRegisters.forEach(r -> r.register(this, workflow));
        StaticWorkflowProcess process = new StaticWorkflowProcess(this, handlers, processInstancesFactory, ServerlessWorkflowParser
                .of(workflow, JavaKogitoBuildContext.builder().withApplicationProperties(System.getProperties()).build()).getProcessInfo().info());
        processRegisters.forEach(r -> r.register(this, workflow, process));
        WorkflowProcessImpl workflowProcess = (WorkflowProcessImpl) process.get();
        workflowProcess.getNodesRecursively().forEach(node -> {
            if (node instanceof SubProcessNode) {
                SubProcessNode subProcess = (SubProcessNode) node;
                subProcess.setSubProcessFactory(new StaticSubprocessFactory((Process<JsonNodeModel>) processes.processById(subProcess.getProcessId())));
            }
        });
        EventFactoryUtils.ready();
        return process;
    }

    @Override
    public <T extends KogitoEngine> T get(Class<T> clazz) {
        if (Processes.class.isAssignableFrom(clazz)) {
            return clazz.cast(processes);
        }
        return super.get(clazz);
    }

    private class StaticWorkflowProcesses implements Processes {
        private Map<String, Process<JsonNodeModel>> map = new ConcurrentHashMap<>();

        @Override
        public Process<? extends Model> processById(String processId) {
            return map.get(processId);
        }

        @Override
        public Collection<String> processIds() {
            return map.keySet();
        }
    }

    @Override
    public void close() {
        processRegisters.forEach(StaticProcessRegister::close);
        workflowRegisters.forEach(StaticWorkflowRegister::close);
        applicationRegisters.forEach(StaticApplicationRegister::close);
        closeables.forEach(t -> {
            try {
                t.close();
            } catch (Exception e) {
                logger.warn("Error closing resource", e);
            }
        });
    }
}
