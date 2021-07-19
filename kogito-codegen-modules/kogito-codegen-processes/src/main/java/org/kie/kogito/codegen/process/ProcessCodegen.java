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
package org.kie.kogito.codegen.process;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.drools.core.io.impl.FileSystemResource;
import org.drools.core.util.StringUtils;
import org.drools.core.xml.SemanticModules;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.compiler.canonical.ModelMetaData;
import org.jbpm.compiler.canonical.ProcessMetaData;
import org.jbpm.compiler.canonical.ProcessToExecModelGenerator;
import org.jbpm.compiler.canonical.TriggerMetaData;
import org.jbpm.compiler.canonical.UserTaskModelMetaData;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.process.core.validation.ProcessValidationError;
import org.jbpm.process.core.validation.ProcessValidatorRegistry;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.api.io.Resource;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.GeneratedFileType;
import org.kie.kogito.codegen.api.context.ContextAttributesConstants;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.core.AbstractGenerator;
import org.kie.kogito.codegen.process.config.ProcessConfigGenerator;
import org.kie.kogito.codegen.process.events.CloudEventMetaFactoryGenerator;
import org.kie.kogito.codegen.process.events.CloudEventsResourceGenerator;
import org.kie.kogito.codegen.process.openapi.OpenApiClientWorkItemIntrospector;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;
import org.kie.kogito.process.validation.ValidationContext;
import org.kie.kogito.process.validation.ValidationException;
import org.kie.kogito.process.validation.ValidationLogDecorator;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

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
    public static final Set<String> SUPPORTED_BPMN_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(".bpmn", ".bpmn2")));
    private static final String YAML_PARSER = "yml";
    private static final String JSON_PARSER = "json";
    public static final String SVG_EXPORT_NAME_EXPRESION = "%s-svg.svg";
    public static final Map<String, String> SUPPORTED_SW_EXTENSIONS;

    static {
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNExtensionsSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNDISemanticModule());

        Map<String, String> extMap = new HashMap<>();
        extMap.put(".sw.yml", YAML_PARSER);
        extMap.put(".sw.yaml", YAML_PARSER);
        extMap.put(".sw.json", JSON_PARSER);
        SUPPORTED_SW_EXTENSIONS = Collections.unmodifiableMap(extMap);
    }

    private final List<ProcessGenerator> processGenerators = new ArrayList<>();

    public static ProcessCodegen ofCollectedResources(KogitoBuildContext context, Collection<CollectedResource> resources) {
        Map<String, String> processSVGMap = new HashMap<>();
        boolean useSvgAddon = context.getAddonsConfig().useProcessSVG();
        final List<Process> processes = resources.stream()
                .map(CollectedResource::resource)
                .flatMap(resource -> {
                    if (SUPPORTED_BPMN_EXTENSIONS.stream().anyMatch(resource.getSourcePath()::endsWith)) {
                        try {
                            Collection<Process> p = parseProcessFile(resource);
                            if (useSvgAddon && resource instanceof FileSystemResource) {
                                processSVG((FileSystemResource) resource, resources, p, processSVGMap);
                            }
                            return p.stream();
                        } catch (ValidationException e) {
                            //TODO: add all errors during parsing phase in the ValidationContext itself
                            ValidationContext.get()
                                    .add(resource.getSourcePath(), e.getErrors())
                                    .putException(e);
                            return Stream.empty();
                        }
                    } else {
                        return SUPPORTED_SW_EXTENSIONS.entrySet()
                                .stream()
                                .filter(e -> resource.getSourcePath().endsWith(e.getKey()))
                                .map(e -> parseWorkflowFile(resource, e.getValue()));
                    }
                })
                //Validate parsed processes
                .map(ProcessCodegen::validate)
                .collect(toList());

        if (useSvgAddon) {
            context.addContextAttribute(ContextAttributesConstants.PROCESS_AUTO_SVG_MAPPING, processSVGMap);
        }

        handleValidation();

        return ofProcesses(context, processes);
    }

    private static void handleValidation() {
        ValidationContext validationContext = ValidationContext.get();
        if (validationContext.hasErrors()) {
            //we may provide different validation decorators, for now just in logging in the console
            ValidationLogDecorator decorator = ValidationLogDecorator
                    .of(validationContext)
                    .decorate();
            Optional<Exception> cause = validationContext.exception();
            validationContext.clear();
            //rethrow exception to break the flow after decoration
            throw new ProcessCodegenException(decorator.simpleMessage(), cause);
        }
    }

    private static Process validate(Process process) {
        ProcessValidationError[] errors = ProcessValidatorRegistry.getInstance().getValidator(process, process.getResource()).validateProcess(process);
        //TODO: use the validation context in the ProcessValidator itself
        Arrays.stream(errors).forEach(e -> ValidationContext.get().add(process.getId(), e));
        return process;
    }

    private static void processSVG(FileSystemResource resource, Collection<CollectedResource> resources,
            Collection<Process> processes, Map<String, String> processSVGMap) {
        File f = resource.getFile();
        String processFileCompleteName = f.getName();
        String fileName = processFileCompleteName.substring(0, processFileCompleteName.lastIndexOf("."));
        processes.stream().forEach(process -> {
            if (isFilenameValid(process.getId() + ".svg")) {
                resources.stream()
                        .filter(r -> r.resource().getSourcePath().endsWith(String.format(SVG_EXPORT_NAME_EXPRESION, fileName)))
                        .forEach(svg -> {
                            try {
                                processSVGMap.put(process.getId(),
                                        new String(Files.readAllBytes(Paths.get(svg.resource().getSourcePath()))));
                            } catch (IOException e) {
                                LOGGER.error("\n IOException trying to add " + svg.resource().getSourcePath() +
                                        " with processId:" + process.getId() + "\n" + e.getMessage(), e);
                            }
                        });
            }
        });
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

    private static ProcessCodegen ofProcesses(KogitoBuildContext context, List<Process> processes) {
        return new ProcessCodegen(context, processes);
    }

    // used on tests only, do not expose
    static List<Process> parseProcesses(Collection<File> processFiles) {
        List<Process> processes = new ArrayList<>();
        for (File processSourceFile : processFiles) {
            try {
                FileSystemResource r = new FileSystemResource(processSourceFile);
                if (SUPPORTED_BPMN_EXTENSIONS.stream().anyMatch(processSourceFile.getPath()::endsWith)) {
                    processes.addAll(parseProcessFile(r));
                } else {
                    SUPPORTED_SW_EXTENSIONS.entrySet()
                            .stream()
                            .filter(e -> processSourceFile.getPath().endsWith(e.getKey()))
                            .forEach(e -> processes.add(parseWorkflowFile(r, e.getValue())));
                }
                if (processes.isEmpty()) {
                    throw new IllegalArgumentException("Unable to process file with unsupported extension: " + processSourceFile);
                }
            } catch (RuntimeException e) {
                throw new ProcessCodegenException(processSourceFile.getAbsolutePath(), e);
            }
        }
        return processes;
    }

    private static Process parseWorkflowFile(Resource r, String parser) {
        try (Reader reader = r.getReader()) {
            ServerlessWorkflowParser workflowParser = ServerlessWorkflowParser.of(reader, parser);
            return workflowParser.getProcess();
        } catch (IOException e) {
            throw new ProcessParsingException("Could not parse file " + r.getSourcePath(), e);
        } catch (RuntimeException e) {
            throw new ProcessCodegenException(r.getSourcePath(), e);
        }
    }

    private static Collection<Process> parseProcessFile(Resource r) {
        try (Reader reader = r.getReader()) {
            XmlProcessReader xmlReader = new XmlProcessReader(
                    BPMN_SEMANTIC_MODULES,
                    Thread.currentThread().getContextClassLoader());
            return xmlReader.read(reader);
        } catch (SAXException | IOException e) {
            throw new ProcessParsingException("Could not parse file " + r.getSourcePath(), e);
        }
    }

    private final Map<String, KogitoWorkflowProcess> processes;
    private final Set<GeneratedFile> generatedFiles = new HashSet<>();

    public ProcessCodegen(KogitoBuildContext context, Collection<? extends Process> processes) {
        super(context, GENERATOR_NAME, new ProcessConfigGenerator(context));
        this.processes = new HashMap<>();
        for (Process process : processes) {
            if (this.processes.containsKey(process.getId())) {
                throw new ProcessCodegenException(format("Duplicated process with id %s found in the project, please review .bpmn files", process.getId()));
            }
            this.processes.put(process.getId(), (KogitoWorkflowProcess) process);
        }
    }

    public static String defaultWorkItemHandlerConfigClass(String packageName) {
        return packageName + ".WorkItemHandlerConfig";
    }

    public static String defaultProcessListenerConfigClass(String packageName) {
        return packageName + ".ProcessEventListenerConfig";
    }

    @Override
    public Collection<GeneratedFile> generate() {
        if (processes.isEmpty()) {
            return Collections.emptySet();
        }

        List<ProcessGenerator> ps = new ArrayList<>();
        List<ProcessInstanceGenerator> pis = new ArrayList<>();
        List<ProcessExecutableModelGenerator> processExecutableModelGenerators = new ArrayList<>();
        List<ProcessResourceGenerator> rgs = new ArrayList<>(); // REST resources
        List<MessageDataEventGenerator> mdegs = new ArrayList<>(); // message data events
        List<MessageConsumerGenerator> megs = new ArrayList<>(); // message endpoints/consumers
        List<MessageProducerGenerator> mpgs = new ArrayList<>(); // message producers

        Map<String, ModelClassGenerator> processIdToModelGenerator = new HashMap<>();
        Map<String, InputModelClassGenerator> processIdToInputModelGenerator = new HashMap<>();
        Map<String, OutputModelClassGenerator> processIdToOutputModelGenerator = new HashMap<>();

        Map<String, List<UserTaskModelMetaData>> processIdToUserTaskModel = new HashMap<>();
        Map<String, ProcessMetaData> processIdToMetadata = new HashMap<>();

        OpenApiClientWorkItemIntrospector introspector = new OpenApiClientWorkItemIntrospector(this.context());

        // first we generate all the data classes from variable declarations
        for (WorkflowProcess workFlowProcess : processes.values()) {
            ModelClassGenerator mcg = new ModelClassGenerator(context(), workFlowProcess);
            processIdToModelGenerator.put(workFlowProcess.getId(), mcg);

            InputModelClassGenerator imcg = new InputModelClassGenerator(context(), workFlowProcess);
            processIdToInputModelGenerator.put(workFlowProcess.getId(), imcg);

            OutputModelClassGenerator omcg = new OutputModelClassGenerator(context(), workFlowProcess);
            processIdToOutputModelGenerator.put(workFlowProcess.getId(), omcg);
        }

        // then we generate user task inputs and outputs if any
        for (WorkflowProcess workFlowProcess : processes.values()) {
            UserTasksModelClassGenerator utcg = new UserTasksModelClassGenerator(workFlowProcess);
            processIdToUserTaskModel.put(workFlowProcess.getId(), utcg.generate());
        }

        // then we can instantiate the exec model generator
        // with the data classes that we have already resolved
        ProcessToExecModelGenerator execModelGenerator =
                new ProcessToExecModelGenerator(context().getClassLoader());

        // collect all process descriptors (exec model)
        for (KogitoWorkflowProcess workFlowProcess : processes.values()) {
            introspector.introspect(workFlowProcess);
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
            String classPrefix = StringUtils.ucFirst(execModelGen.extractedProcessId());
            KogitoWorkflowProcess workFlowProcess = execModelGen.process();
            ModelClassGenerator modelClassGenerator =
                    processIdToModelGenerator.get(execModelGen.getProcessId());

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

            //Creating and adding the ResourceGenerator
            ProcessResourceGenerator processResourceGenerator = new ProcessResourceGenerator(
                    context(),
                    workFlowProcess,
                    modelClassGenerator.className(),
                    execModelGen.className(),
                    applicationCanonicalName());

            processResourceGenerator
                    .withUserTasks(processIdToUserTaskModel.get(workFlowProcess.getId()))
                    .withSignals(metaData.getSignals())
                    .withTriggers(metaData.isStartable(), metaData.isDynamic());

            rgs.add(processResourceGenerator);

            if (metaData.getTriggers() != null) {

                for (TriggerMetaData trigger : metaData.getTriggers()) {

                    // generate message consumers for processes with message start events
                    if (trigger.getType().equals(TriggerMetaData.TriggerType.ConsumeMessage)) {

                        MessageDataEventGenerator msgDataEventGenerator =
                                new MessageDataEventGenerator(context(), workFlowProcess, trigger);
                        mdegs.add(msgDataEventGenerator);

                        megs.add(new MessageConsumerGenerator(
                                context(),
                                workFlowProcess,
                                modelClassGenerator.className(),
                                execModelGen.className(),
                                applicationCanonicalName(),
                                msgDataEventGenerator.className(),
                                trigger));
                    } else if (trigger.getType().equals(TriggerMetaData.TriggerType.ProduceMessage)) {

                        MessageDataEventGenerator msgDataEventGenerator =
                                new MessageDataEventGenerator(context(), workFlowProcess, trigger);
                        mdegs.add(msgDataEventGenerator);
                        mpgs.add(new MessageProducerGenerator(
                                context(),
                                workFlowProcess,
                                modelClassGenerator.className(),
                                execModelGen.className(),
                                msgDataEventGenerator.className(),
                                trigger));
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

        for (List<UserTaskModelMetaData> utmd : processIdToUserTaskModel.values()) {

            for (UserTaskModelMetaData ut : utmd) {
                storeFile(MODEL_TYPE, UserTasksModelClassGenerator.generatedFilePath(ut.getInputModelClassName()), ut.generateInput());

                storeFile(MODEL_TYPE, UserTasksModelClassGenerator.generatedFilePath(ut.getOutputModelClassName()), ut.generateOutput());

                storeFile(MODEL_TYPE, UserTasksModelClassGenerator.generatedFilePath(ut.getTaskModelClassName()), ut.generateModel());
            }
        }

        if (context().hasREST()) {
            for (ProcessResourceGenerator resourceGenerator : rgs) {
                storeFile(REST_TYPE, resourceGenerator.generatedFilePath(),
                        resourceGenerator.generate());
                storeFile(MODEL_TYPE, UserTasksModelClassGenerator.generatedFilePath(resourceGenerator.getTaskModelFactoryClassName()), resourceGenerator.getTaskModelFactory());
            }
            //Generating the Producer classes for Dependency Injection
            StaticDependencyInjectionProducerGenerator.of(context())
                    .generate()
                    .entrySet()
                    .forEach(entry -> storeFile(PRODUCER_TYPE, entry.getKey(), entry.getValue()));
        }

        for (MessageDataEventGenerator messageDataEventGenerator : mdegs) {
            storeFile(GeneratedFileType.SOURCE, messageDataEventGenerator.generatedFilePath(),
                    messageDataEventGenerator.generate());
        }

        for (MessageConsumerGenerator messageConsumerGenerator : megs) {
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

        // Generic CloudEvents HTTP Endpoint will be handled by https://issues.redhat.com/browse/KOGITO-2956
        if (context().getAddonsConfig().useCloudEvents() && context().hasREST() && QuarkusKogitoBuildContext.CONTEXT_NAME.equals(context().name())) {
            final CloudEventsResourceGenerator ceGenerator =
                    new CloudEventsResourceGenerator(context(), processExecutableModelGenerators);
            storeFile(REST_TYPE, ceGenerator.generatedFilePath(), ceGenerator.generate());
        }
        if ((context().getAddonsConfig().useProcessSVG())) {
            Map<String, String> svgs = context().getContextAttribute(ContextAttributesConstants.PROCESS_AUTO_SVG_MAPPING, Map.class);
            svgs.keySet().stream().forEach(key -> storeFile(GeneratedFileType.RESOURCE, "META-INF/processSVG/" + key + ".svg", svgs.get(key)));
        }

        if (context().hasREST()) {
            final CloudEventMetaFactoryGenerator topicsGenerator =
                    new CloudEventMetaFactoryGenerator(context(), processExecutableModelGenerators);
            storeFile(REST_TYPE, topicsGenerator.generatedFilePath(), topicsGenerator.generate());
        }

        for (ProcessInstanceGenerator pi : pis) {
            storeFile(PROCESS_INSTANCE_TYPE, pi.generatedFilePath(), pi.generate());
        }
        return generatedFiles;
    }

    private void storeFile(GeneratedFileType type, String path, String source) {
        if (generatedFiles.stream().anyMatch(f -> path.equals(f.relativePath()))) {
            LOGGER.warn("There's already a generated file named {} to be compiled. Ignoring.", path);
        } else {
            generatedFiles.add(new GeneratedFile(type, path, source));
        }
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
}
