/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.codegen.process;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Stream;

import org.drools.codegen.common.GeneratedFile;
import org.drools.codegen.common.GeneratedFileType;
import org.drools.io.InternalResource;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.compiler.canonical.*;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.compiler.xml.core.SemanticModules;
import org.jbpm.process.core.impl.ProcessImpl;
import org.jbpm.process.core.validation.ProcessValidatorRegistry;
import org.jbpm.workflow.core.impl.WorkflowProcessImpl;
import org.jbpm.workflow.instance.WorkflowProcessParameters;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.api.io.Resource;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.GeneratedInfo;
import org.kie.kogito.codegen.api.SourceFileCodegenBindEvent;
import org.kie.kogito.codegen.api.context.ContextAttributesConstants;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.codegen.core.AbstractGenerator;
import org.kie.kogito.codegen.core.DashboardGeneratedFileUtils;
import org.kie.kogito.codegen.process.config.ProcessConfigGenerator;
import org.kie.kogito.codegen.process.events.ProcessCloudEventMeta;
import org.kie.kogito.codegen.process.events.ProcessCloudEventMetaFactoryGenerator;
import org.kie.kogito.codegen.process.util.CodegenUtil;
import org.kie.kogito.internal.SupportedExtensions;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;
import org.kie.kogito.process.validation.ValidationException;
import org.kie.kogito.process.validation.ValidationLogDecorator;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;
import org.kie.kogito.serverless.workflow.utils.WorkflowFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import static com.github.javaparser.StaticJavaParser.parse;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.jbpm.process.core.constants.CalendarConstants.BUSINESS_CALENDAR_PATH;
import static org.kie.kogito.codegen.process.util.BusinessCalendarUtil.conditionallyAddCustomBusinessCalendar;
import static org.kie.kogito.codegen.process.util.CodegenUtil.generatorProperty;
import static org.kie.kogito.codegen.process.util.CodegenUtil.isFaultToleranceEnabled;
import static org.kie.kogito.codegen.process.util.CodegenUtil.isTransactionEnabled;
import static org.kie.kogito.codegen.process.util.SourceFilesProviderProducerUtil.addSourceFilesToProvider;
import static org.kie.kogito.grafana.GrafanaConfigurationWriter.buildDashboardName;
import static org.kie.kogito.grafana.GrafanaConfigurationWriter.generateOperationalDashboard;
import static org.kie.kogito.internal.utils.ConversionUtils.sanitizeClassName;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.FAIL_ON_ERROR_PROPERTY;

/**
 * Entry point to process code generation
 */
public class ProcessCodegen extends AbstractGenerator {

    public static final String GENERATOR_NAME = "processes";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCodegen.class);

    private static final GeneratedFileType PROCESS_TYPE = GeneratedFileType.of("PROCESS", GeneratedFileType.Category.SOURCE);
    private static final GeneratedFileType PROCESS_INSTANCE_TYPE = GeneratedFileType.of("PROCESS_INSTANCE", GeneratedFileType.Category.SOURCE);
    private static final GeneratedFileType MESSAGE_PRODUCER_TYPE = GeneratedFileType.of("MESSAGE_PRODUCER", GeneratedFileType.Category.SOURCE);
    private static final GeneratedFileType MESSAGE_CONSUMER_TYPE = GeneratedFileType.of("MESSAGE_CONSUMER", GeneratedFileType.Category.SOURCE);
    private static final GeneratedFileType PRODUCER_TYPE = GeneratedFileType.of("PRODUCER", GeneratedFileType.Category.SOURCE);
    private static final SemanticModules BPMN_SEMANTIC_MODULES = new SemanticModules();
    public static final String SVG_EXPORT_NAME_EXPRESION = "%s-svg.svg";
    public static final String CUSTOM_BUSINESS_CALENDAR_PROPERTY = "businessCalendar";

    private static final String GLOBAL_OPERATIONAL_DASHBOARD_TEMPLATE = "/grafana-dashboard-template/processes/global-operational-dashboard-template.json";
    private static final String PROCESS_OPERATIONAL_DASHBOARD_TEMPLATE = "/grafana-dashboard-template/processes/process-operational-dashboard-template.json";
    public static final String BUSINESS_CALENDAR_PRODUCER_TEMPLATE = "BusinessCalendarProducer";
    public static final String SOURCE_FILE_PROVIDER_PRODUCER = "SourceFilesProviderProducer";

    private static final String IS_BUSINESS_CALENDAR_PRESENT = "isBusinessCalendarPresent";

    static {
        ProcessValidatorRegistry.getInstance().registerAdditonalValidator(JavaRuleFlowProcessValidator.getInstance());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNExtensionsSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNDISemanticModule());
    }

    private final List<ProcessGenerator> processGenerators = new ArrayList<>();

    public static ProcessCodegen ofCollectedResources(KogitoBuildContext context, Collection<CollectedResource> resources) {
        Map<String, byte[]> processSVGMap = new HashMap<>();
        Map<String, Throwable> processesErrors = new HashMap<>();
        boolean useSvgAddon = context.getAddonsConfig().useProcessSVG();
        final List<GeneratedInfo<KogitoWorkflowProcess>> processes = resources.stream()
                .map(CollectedResource::resource)
                .flatMap(resource -> {
                    try {
                        if (SupportedExtensions.getBPMNExtensions().stream().anyMatch(resource.getSourcePath()::endsWith)) {
                            Collection<Process> p = parseProcessFile(resource);
                            notifySourceFileCodegenBindListeners(context, resource, p);
                            if (useSvgAddon) {
                                processSVG(resource, resources, p, processSVGMap);
                            }
                            return p.stream().map(KogitoWorkflowProcess.class::cast).map(GeneratedInfo::new).map(info -> addResource(info, resource));
                        } else if (SupportedExtensions.getSWFExtensions().stream().anyMatch(resource.getSourcePath()::endsWith)) {
                            GeneratedInfo<KogitoWorkflowProcess> generatedInfo = parseWorkflowFile(resource, context);
                            notifySourceFileCodegenBindListeners(context, resource, Collections.singletonList(generatedInfo.info()));
                            return Stream.of(addResource(generatedInfo, resource));
                        }
                    } catch (ValidationException e) {
                        processesErrors.put(resource.getSourcePath(), e);
                    } catch (ProcessParsingException e) {
                        processesErrors.put(resource.getSourcePath(), e.getCause());
                    }
                    return Stream.empty();
                })
                //Validate parsed processes
                .map(processInfo -> validate(processInfo, processesErrors))
                .collect(toList());

        if (useSvgAddon) {
            context.addContextAttribute(ContextAttributesConstants.PROCESS_AUTO_SVG_MAPPING, processSVGMap);
        }
        context.addContextAttribute(IS_BUSINESS_CALENDAR_PRESENT, resources.stream().anyMatch(resource -> resource.resource().getSourcePath().endsWith(BUSINESS_CALENDAR_PATH)));

        handleValidation(context, processesErrors);

        return ofProcesses(context, processes);
    }

    private static GeneratedInfo<KogitoWorkflowProcess> addResource(GeneratedInfo<KogitoWorkflowProcess> info, Resource r) {
        ((ProcessImpl) info.info()).setResource(r);
        return info;
    }

    private static void notifySourceFileCodegenBindListeners(KogitoBuildContext context, Resource resource, Collection<Process> processes) {
        context.getSourceFileCodegenBindNotifier()
                .ifPresent(notifier -> processes.forEach(p -> notifier.notify(new SourceFileCodegenBindEvent(p.getId(), resource.getSourcePath()))));
    }

    private static void handleValidation(KogitoBuildContext context, Map<String, Throwable> processesErrors) {
        if (!processesErrors.isEmpty()) {
            ValidationLogDecorator decorator = new ValidationLogDecorator(processesErrors);
            decorator.decorate();
            //rethrow exception to break the flow after decoration unless property is set to false
            if (context.getApplicationProperty(FAIL_ON_ERROR_PROPERTY, Boolean.class).orElse(true)) {
                throw new ProcessCodegenException("Processes with errors are " + decorator.toString());
            }
        }
    }

    private static GeneratedInfo<KogitoWorkflowProcess> validate(GeneratedInfo<KogitoWorkflowProcess> processInfo, Map<String, Throwable> processesErrors) {
        Process process = processInfo.info();
        try {
            ProcessValidatorRegistry.getInstance().getValidator(process, process.getResource()).validate(process);
        } catch (ValidationException e) {
            processesErrors.put(process.getResource().getSourcePath(), e);
        }
        return processInfo;
    }

    private static void processSVG(Resource resource, Collection<CollectedResource> resources,
            Collection<Process> processes, Map<String, byte[]> processSVGMap) {
        String sourcePath = resource.getSourcePath();
        if (sourcePath != null) {
            String fileName = sourcePath.substring(0, sourcePath.lastIndexOf("."));
            processes.stream().forEach(process -> {
                if (isFilenameValid(process.getId() + ".svg")) {
                    resources.stream()
                            .filter(r -> r.resource().getSourcePath().endsWith(String.format(SVG_EXPORT_NAME_EXPRESION, fileName)))
                            .forEach(svg -> {
                                try {
                                    processSVGMap.put(process.getId(),
                                            svg.resource().getInputStream().readAllBytes());
                                } catch (IOException e) {
                                    LOGGER.error("\n IOException trying to add " + svg.resource().getSourcePath() +
                                            " with processId:" + process.getId() + "\n" + e.getMessage(), e);
                                }
                            });
                }
            });
        }
    }

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static ProcessCodegen ofProcesses(KogitoBuildContext context, List<GeneratedInfo<KogitoWorkflowProcess>> processes) {
        return new ProcessCodegen(context, processes);
    }

    protected static GeneratedInfo<KogitoWorkflowProcess> parseWorkflowFile(Resource r, KogitoBuildContext context) {
        InternalResource resource = (InternalResource) r;
        try (Reader reader = resource.getReader()) {
            ServerlessWorkflowParser parser = ServerlessWorkflowParser.of(reader, WorkflowFormat.fromFileName(resource.getSourcePath()), context);
            if (resource.hasURL()) {
                parser.withBaseURI(resource.getURL());
            } else {
                parser.withBaseURI("classpath:" + resource.getSourcePath());
            }
            return parser.getProcessInfo();
        } catch (Exception e) {
            throw new ProcessParsingException(e);
        }
    }

    protected static Collection<Process> parseProcessFile(Resource r) {
        try (Reader reader = r.getReader()) {
            XmlProcessReader xmlReader = new XmlProcessReader(
                    BPMN_SEMANTIC_MODULES,
                    Thread.currentThread().getContextClassLoader());
            return xmlReader.read(reader);
        } catch (SAXException | IOException e) {
            throw new ProcessParsingException(e);
        }
    }

    private final Map<String, KogitoWorkflowProcess> processes;
    private final Set<GeneratedFile> generatedFiles = new HashSet<>();

    protected ProcessCodegen(KogitoBuildContext context, Collection<GeneratedInfo<KogitoWorkflowProcess>> processes) {
        super(context, GENERATOR_NAME, new ProcessConfigGenerator(context));
        this.processes = new HashMap<>();
        for (GeneratedInfo<KogitoWorkflowProcess> process : processes) {
            if (this.processes.containsKey(process.info().getId())) {
                throw new ProcessCodegenException(
                        format("Duplicated item found with id %s. Please review the .%s files",
                                process.info().getId(),
                                process.info().getType().toLowerCase()));
            }
            generatedFiles.addAll(process.files());
            this.processes.put(process.info().getId(), process.info());
        }
    }

    public static String defaultWorkItemHandlerConfigClass(String packageName) {
        return packageName + ".WorkItemHandlerConfig";
    }

    public static String defaultProcessListenerConfigClass(String packageName) {
        return packageName + ".ProcessEventListenerConfig";
    }

    private boolean skipModelGeneration(WorkflowProcess process) {
        return process.getType().equals(KogitoWorkflowProcess.SW_TYPE);
    }

    @Override
    protected Collection<GeneratedFile> internalGenerate() {

        List<ProcessGenerator> ps = new ArrayList<>();
        List<ProcessInstanceGenerator> pis = new ArrayList<>();
        List<ProcessExecutableModelGenerator> processExecutableModelGenerators = new ArrayList<>();
        List<ProcessResourceGenerator> rgs = new ArrayList<>(); // REST resources
        Map<ProcessCloudEventMeta, MessageConsumerGenerator> megs = new HashMap<>(); // message endpoints/consumers
        List<MessageProducerGenerator> mpgs = new ArrayList<>(); // message producers

        Map<String, ModelClassGenerator> processIdToModelGenerator = new HashMap<>();
        Map<String, InputModelClassGenerator> processIdToInputModelGenerator = new HashMap<>();
        Map<String, OutputModelClassGenerator> processIdToOutputModelGenerator = new HashMap<>();

        Map<String, List<WorkItemModelMetaData>> processIdToWorkItemModel = new HashMap<>();
        Map<String, ProcessMetaData> processIdToMetadata = new HashMap<>();

        // first we generate all the data classes from variable declarations
        for (WorkflowProcess workFlowProcess : processes.values()) {
            // transaction is disabled by default for SW types
            boolean defaultTransactionEnabled = !KogitoWorkflowProcess.SW_TYPE.equals(workFlowProcess.getType());
            if (isTransactionEnabled(this, context(), defaultTransactionEnabled)) {
                ((WorkflowProcessImpl) workFlowProcess).setMetaData(WorkflowProcessParameters.WORKFLOW_PARAM_TRANSACTIONS.getName(), "true");
            }

            if (!skipModelGeneration(workFlowProcess)) {
                ModelClassGenerator mcg = new ModelClassGenerator(context(), workFlowProcess);
                processIdToModelGenerator.put(workFlowProcess.getId(), mcg);

                InputModelClassGenerator imcg = new InputModelClassGenerator(context(), workFlowProcess);
                processIdToInputModelGenerator.put(workFlowProcess.getId(), imcg);

                OutputModelClassGenerator omcg = new OutputModelClassGenerator(context(), workFlowProcess);
                processIdToOutputModelGenerator.put(workFlowProcess.getId(), omcg);
            }
        }
        boolean isServerless = false;
        // then we generate work items task inputs and outputs if any
        for (WorkflowProcess workFlowProcess : processes.values()) {
            isServerless |= KogitoWorkflowProcess.SW_TYPE.equals(workFlowProcess.getType());
            if (KogitoWorkflowProcess.SW_TYPE.equals(workFlowProcess.getType())) {
                continue;
            }
            WorkItemModelClassGenerator utcg = new WorkItemModelClassGenerator(workFlowProcess);
            processIdToWorkItemModel.put(workFlowProcess.getId(), utcg.generate());
        }

        // then we can instantiate the exec model generator
        // with the data classes that we have already resolved
        ProcessToExecModelGenerator execModelGenerator =
                new ProcessToExecModelGenerator(context().getClassLoader());

        // collect all process descriptors (exec model)
        for (KogitoWorkflowProcess workFlowProcess : processes.values()) {
            ProcessExecutableModelGenerator execModelGen =
                    new ProcessExecutableModelGenerator(workFlowProcess, execModelGenerator);
            String packageName = workFlowProcess.getPackageName();
            String id = workFlowProcess.getId();
            try {
                ProcessMetaData generate = execModelGen.generate();
                processIdToMetadata.put(id, generate);
                processExecutableModelGenerators.add(execModelGen);
            } catch (RuntimeException e) {
                throw new ProcessCodegenException(id, packageName, e);
            }
        }

        // generate Process, ProcessInstance classes and the REST resource

        for (ProcessExecutableModelGenerator execModelGen : processExecutableModelGenerators) {
            String classPrefix = sanitizeClassName(execModelGen.extractedProcessId());
            KogitoWorkflowProcess workFlowProcess = execModelGen.process();
            ModelClassGenerator modelClassGenerator =
                    processIdToModelGenerator.getOrDefault(execModelGen.getProcessId(), new ModelClassGenerator(context(), workFlowProcess));

            ProcessGenerator p = new ProcessGenerator(
                    context(),
                    workFlowProcess,
                    execModelGen,
                    classPrefix,
                    modelClassGenerator.className(),
                    applicationCanonicalName());

            ProcessInstanceGenerator pi = new ProcessInstanceGenerator(
                    workFlowProcess.getPackageName(),
                    classPrefix,
                    modelClassGenerator.generate());

            ProcessMetaData metaData = processIdToMetadata.get(workFlowProcess.getId());

            //Creating and adding the ResourceGenerator for REST generation
            if (context().hasRest()) {
                ProcessResourceGenerator processResourceGenerator = new ProcessResourceGenerator(
                        context(),
                        workFlowProcess,
                        modelClassGenerator.className(),
                        execModelGen.className(),
                        applicationCanonicalName());

                processResourceGenerator
                        .withWorkItems(processIdToWorkItemModel.get(workFlowProcess.getId()))
                        .withSignals(metaData.getSignals())
                        .withTriggers(metaData.isStartable(), metaData.isDynamic(), metaData.getTriggers())
                        .withTransaction(isTransactionEnabled(this, context()))
                        .withFaultTolerance(isFaultToleranceEnabled(context()));

                rgs.add(processResourceGenerator);
            }

            if (metaData.getTriggers() != null) {

                for (TriggerMetaData trigger : metaData.getTriggers()) {

                    // generate message consumers for processes with message start events
                    if (trigger.getType().equals(TriggerMetaData.TriggerType.ConsumeMessage)) {
                        MessageConsumerGenerator messageConsumerGenerator =
                                megs.computeIfAbsent(new ProcessCloudEventMeta(workFlowProcess.getId(), trigger), k -> new MessageConsumerGenerator(
                                        context(),
                                        workFlowProcess,
                                        modelClassGenerator.className(),
                                        execModelGen.className(),
                                        applicationCanonicalName(),
                                        trigger));
                        metaData.addConsumer(trigger.getName(), messageConsumerGenerator.compilationUnit());
                    } else if (trigger.getType().equals(TriggerMetaData.TriggerType.ProduceMessage)) {
                        MessageProducerGenerator messageProducerGenerator = new MessageProducerGenerator(
                                context(),
                                workFlowProcess,
                                trigger);
                        mpgs.add(messageProducerGenerator);
                        metaData.addProducer(trigger.getName(), messageProducerGenerator.compilationUnit());
                    }
                }
            }

            processGenerators.add(p);

            ps.add(p);
            pis.add(pi);
        }

        for (ModelClassGenerator modelClassGenerator : processIdToModelGenerator.values()) {
            ModelMetaData mmd = modelClassGenerator.generate();
            storeFile(MODEL_TYPE, modelClassGenerator.generatedFilePath(),
                    mmd.generate());
        }

        for (InputModelClassGenerator modelClassGenerator : processIdToInputModelGenerator.values()) {
            ModelMetaData mmd = modelClassGenerator.generate();
            storeFile(MODEL_TYPE, modelClassGenerator.generatedFilePath(),
                    mmd.generate());
        }

        for (OutputModelClassGenerator modelClassGenerator : processIdToOutputModelGenerator.values()) {
            ModelMetaData mmd = modelClassGenerator.generate();
            storeFile(MODEL_TYPE, modelClassGenerator.generatedFilePath(),
                    mmd.generate());
        }

        for (List<WorkItemModelMetaData> utmd : processIdToWorkItemModel.values()) {

            for (WorkItemModelMetaData ut : utmd) {
                storeFile(MODEL_TYPE, WorkItemModelClassGenerator.generatedFilePath(ut.getInputModelClassName()), ut.generateInput());

                storeFile(MODEL_TYPE, WorkItemModelClassGenerator.generatedFilePath(ut.getOutputModelClassName()), ut.generateOutput());

                storeFile(MODEL_TYPE, WorkItemModelClassGenerator.generatedFilePath(ut.getTaskModelClassName()), ut.generateModel());
            }
        }

        //Generating the Producer classes for Dependency Injection
        StaticDependencyInjectionProducerGenerator staticDependencyInjectionProducerGenerator = StaticDependencyInjectionProducerGenerator.of(context());

        staticDependencyInjectionProducerGenerator.generate()
                .entrySet()
                .forEach(entry -> storeFile(PRODUCER_TYPE, entry.getKey(), entry.getValue()));

        generateBusinessCalendarProducer();

        generateSourceFileProviderProducer();

        if (CodegenUtil.isTransactionEnabled(this, context()) && !isServerless) {
            String template = "ExceptionHandlerTransaction";
            TemplatedGenerator generator = TemplatedGenerator.builder()
                    .withTemplateBasePath("/class-templates/transaction/")
                    .withFallbackContext(JavaKogitoBuildContext.CONTEXT_NAME)
                    .withTargetTypeName(template)
                    .build(context(), template);
            CompilationUnit handler = generator.compilationUnitOrThrow();
            storeFile(MODEL_TYPE, generator.generatedFilePath(), handler.toString());
        }

        if (context().hasRESTForGenerator(this)) {
            for (ProcessResourceGenerator resourceGenerator : rgs) {
                storeFile(REST_TYPE, resourceGenerator.generatedFilePath(),
                        resourceGenerator.generate());
                storeFile(MODEL_TYPE, WorkItemModelClassGenerator.generatedFilePath(resourceGenerator.getTaskModelFactoryClassName()), resourceGenerator.getTaskModelFactory());
            }
        }

        for (MessageConsumerGenerator messageConsumerGenerator : megs.values()) {
            storeFile(MESSAGE_CONSUMER_TYPE, messageConsumerGenerator.generatedFilePath(),
                    messageConsumerGenerator.generate());
        }

        for (MessageProducerGenerator messageProducerGenerator : mpgs) {
            storeFile(MESSAGE_PRODUCER_TYPE, messageProducerGenerator.generatedFilePath(),
                    messageProducerGenerator.generate());
        }

        for (ProcessGenerator p : ps) {
            storeFile(PROCESS_TYPE, p.generatedFilePath(), p.generate());

            p.getAdditionalClasses().forEach(cp -> {
                String packageName = cp.getPackageDeclaration().map(pd -> pd.getName().toString()).orElse("");
                String clazzName = cp.findFirst(ClassOrInterfaceDeclaration.class).map(cls -> cls.getName().toString()).get();
                String path = (packageName + "." + clazzName).replace('.', '/') + ".java";
                storeFile(GeneratedFileType.SOURCE, path, cp.toString());
            });
        }

        if ((context().getAddonsConfig().useProcessSVG())) {
            Map<String, byte[]> svgs = context().getContextAttribute(ContextAttributesConstants.PROCESS_AUTO_SVG_MAPPING, Map.class);
            svgs.keySet().stream().forEach(key -> storeFile(GeneratedFileType.INTERNAL_RESOURCE, "META-INF/processSVG/" + key + ".svg", svgs.get(key)));
        }

        if (context().hasRest() && context().hasRESTForGenerator(this)) {
            final ProcessCloudEventMetaFactoryGenerator topicsGenerator =
                    new ProcessCloudEventMetaFactoryGenerator(context(), processExecutableModelGenerators);
            storeFile(REST_TYPE, topicsGenerator.generatedFilePath(), topicsGenerator.generate());
        }

        for (ProcessInstanceGenerator pi : pis) {
            storeFile(PROCESS_INSTANCE_TYPE, pi.generatedFilePath(), pi.generate());
        }

        // generate Grafana dashboards
        if (context().getAddonsConfig().usePrometheusMonitoring()) {

            Optional<String> globalDbJson = generateOperationalDashboard(GLOBAL_OPERATIONAL_DASHBOARD_TEMPLATE,
                    "Global",
                    context().getPropertiesMap(),
                    "Global",
                    context().getGAV().orElse(KogitoGAV.EMPTY_GAV),
                    false);
            String globalDbName = buildDashboardName(context().getGAV(), "Global");
            globalDbJson.ifPresent(dashboard -> generatedFiles.addAll(DashboardGeneratedFileUtils.operational(dashboard, globalDbName + ".json")));
            for (KogitoWorkflowProcess process : processes.values()) {
                String dbName = buildDashboardName(context().getGAV(), process.getId());
                Optional<String> dbJson = generateOperationalDashboard(PROCESS_OPERATIONAL_DASHBOARD_TEMPLATE,
                        process.getId(),
                        context().getPropertiesMap(),
                        process.getId(),
                        context().getGAV().orElse(KogitoGAV.EMPTY_GAV),
                        false);
                dbJson.ifPresent(dashboard -> generatedFiles.addAll(DashboardGeneratedFileUtils.operational(dashboard, dbName + ".json")));
            }
        }

        return generatedFiles;
    }

    private void storeFile(GeneratedFileType type, String path, String source) {
        if (generatedFiles.stream().anyMatch(f -> path.equals(f.relativePath()))) {
            LOGGER.warn("There is already a generated file named {} to be compiled. Ignoring.", path);
        } else {
            generatedFiles.add(new GeneratedFile(type, path, source));
        }
    }

    private void storeFile(GeneratedFileType type, String path, byte[] source) {
        if (generatedFiles.stream().anyMatch(f -> path.equals(f.relativePath()))) {
            LOGGER.warn("There is already a generated file named {} to be compiled. Ignoring.", path);
        } else {
            generatedFiles.add(new GeneratedFile(type, path, source));
        }
    }

    @Override
    public boolean isEmpty() {
        return processes.isEmpty();
    }

    public Collection<Process> processes() {
        return Collections.unmodifiableCollection(processes.values());
    }

    @Override
    public Optional<ApplicationSection> section() {
        ProcessContainerGenerator moduleGenerator = new ProcessContainerGenerator(context());
        processGenerators.forEach(moduleGenerator::addProcess);
        return Optional.of(moduleGenerator);
    }

    @Override
    public int priority() {
        return 10;
    }

    private void generateBusinessCalendarProducer() {

        boolean isBusinessCalendarPresent = Optional.ofNullable(context().getContextAttribute(IS_BUSINESS_CALENDAR_PRESENT, Boolean.class)).orElse(false);
        String businessCalendarClassName = CodegenUtil.getProperty(this, context(), CUSTOM_BUSINESS_CALENDAR_PROPERTY, String::valueOf, null);

        if (isBusinessCalendarPresent && businessCalendarClassName != null) {
            String message = String.format("Project could not provide both '%s' file and '%s' property.", BUSINESS_CALENDAR_PATH, generatorProperty(this, CUSTOM_BUSINESS_CALENDAR_PROPERTY));
            throw new ProcessCodegenException(message);
        }

        if (!isBusinessCalendarPresent && businessCalendarClassName == null) {
            return;
        }

        TemplatedGenerator generator = TemplatedGenerator.builder()
                .withTemplateBasePath("/class-templates/producer/")
                .withFallbackContext(JavaKogitoBuildContext.CONTEXT_NAME)
                .withTargetTypeName(BUSINESS_CALENDAR_PRODUCER_TEMPLATE)
                .build(context(), BUSINESS_CALENDAR_PRODUCER_TEMPLATE);

        CompilationUnit compilationUnit = generator.compilationUnitOrThrow();

        if (businessCalendarClassName != null) {
            conditionallyAddCustomBusinessCalendar(compilationUnit, context(), businessCalendarClassName);
        }

        storeFile(PRODUCER_TYPE, generator.generatedFilePath(), compilationUnit.toString());
    }

    private void generateSourceFileProviderProducer() {

        if (!context().getAddonsConfig().useSourceFiles()) {
            return;
        }

        CompilationUnit compilationUnit = parse(this.getClass().getResourceAsStream("/class-templates/producer/" + SOURCE_FILE_PROVIDER_PRODUCER + "Template.java"));
        compilationUnit.setPackageDeclaration(context().getPackageName());

        addSourceFilesToProvider(compilationUnit, processes, context());

        String generatedPath = context().getPackageName().replace(".", "/") + "/" + SOURCE_FILE_PROVIDER_PRODUCER + ".java";

        storeFile(PRODUCER_TYPE, generatedPath, compilationUnit.toString());
    }
}
