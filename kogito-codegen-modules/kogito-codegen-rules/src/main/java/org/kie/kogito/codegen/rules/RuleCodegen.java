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
package org.kie.kogito.codegen.rules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.drools.compiler.compiler.DecisionTableFactory;
import org.drools.compiler.compiler.DroolsError;
import org.drools.modelcompiler.builder.ModelBuilderImpl;
import org.drools.modelcompiler.builder.ModelSourceClass;
import org.drools.ruleunits.impl.AbstractRuleUnitDescription;
import org.drools.ruleunits.impl.AssignableChecker;
import org.drools.ruleunits.impl.ReflectiveRuleUnitDescription;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.CompositeKnowledgeBuilder;
import org.kie.internal.builder.DecisionTableConfiguration;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.GeneratedFileType;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.codegen.core.AbstractGenerator;
import org.kie.kogito.codegen.core.DashboardGeneratedFileUtils;
import org.kie.kogito.codegen.rules.config.NamedRuleUnitConfig;
import org.kie.kogito.codegen.rules.config.RuleConfigGenerator;
import org.kie.kogito.grafana.GrafanaConfigurationWriter;
import org.kie.kogito.rules.RuleUnitConfig;
import org.kie.util.maven.support.ReleaseIdImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static org.drools.compiler.kie.builder.impl.AbstractKieModule.addDTableToCompiler;
import static org.drools.compiler.kie.builder.impl.AbstractKieModule.loadResourceConfiguration;

public class RuleCodegen extends AbstractGenerator {

    public static final GeneratedFileType RULE_TYPE = GeneratedFileType.of("RULE", GeneratedFileType.Category.SOURCE);
    public static final String TEMPLATE_RULE_FOLDER = "/class-templates/rules/";
    public static final String GENERATOR_NAME = "rules";
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleCodegen.class);

    public static RuleCodegen ofCollectedResources(KogitoBuildContext context, Collection<CollectedResource> resources) {
        List<Resource> generatedRules = resources.stream()
                .map(CollectedResource::resource)
                .filter(r -> isRuleFile(r) || r.getResourceType() == ResourceType.PROPERTIES)
                .collect(toList());
        return ofResources(context, generatedRules);
    }

    public static RuleCodegen ofJavaResources(KogitoBuildContext context, Collection<CollectedResource> resources) {
        List<Resource> generatedRules =
                AnnotatedClassPostProcessor.scan(
                        resources.stream()
                                .filter(r -> r.resource().getResourceType() == ResourceType.JAVA)
                                .map(r -> new File(r.resource().getSourcePath()))
                                .map(File::toPath))
                        .generate();
        return ofResources(context, generatedRules);
    }

    public static RuleCodegen ofResources(KogitoBuildContext context, Collection<Resource> resources) {
        return new RuleCodegen(context, resources);
    }

    private static final String operationalDashboardDrlTemplate = "/grafana-dashboard-template/operational-dashboard-template.json";
    private static final String domainDashboardDrlTemplate = "/grafana-dashboard-template/domain-dashboard-template.json";
    private final Collection<Resource> resources;
    private final List<RuleUnitGenerator> ruleUnitGenerators = new ArrayList<>();

    private KieModuleThing kieModuleThing;
    private boolean hotReloadMode = false;
    private final boolean decisionTableSupported;
    private final Map<String, RuleUnitConfig> configs;

    private RuleCodegen(KogitoBuildContext context, Collection<Resource> resources) {
        super(context, GENERATOR_NAME, new RuleConfigGenerator(context));
        this.resources = resources;
        this.kieModuleThing = KieModuleThing.fromContext(context);
        this.decisionTableSupported = DecisionTableFactory.getDecisionTableProvider() != null;
        this.configs = new HashMap<>();
        for (NamedRuleUnitConfig cfg : NamedRuleUnitConfig.fromContext(context)) {
            this.configs.put(cfg.getCanonicalName(), cfg.getConfig());
        }
    }

    @Override
    public Optional<ApplicationSection> section() {
        RuleUnitContainerGenerator moduleGenerator = new RuleUnitContainerGenerator(context());
        ruleUnitGenerators.forEach(moduleGenerator::addRuleUnit);
        return Optional.of(moduleGenerator);
    }

    @Override
    public boolean isEmpty() {
        return resources.isEmpty();
    }

    @Override
    protected Collection<GeneratedFile> internalGenerate() {
        ReleaseIdImpl dummyReleaseId = new ReleaseIdImpl("dummy:dummy:0.0.0");
        if (!decisionTableSupported &&
                resources.stream().anyMatch(r -> r.getResourceType() == ResourceType.DTABLE)) {
            throw new MissingDecisionTableDependencyError();
        }

        ModelBuilderImpl<KogitoPackageSources> modelBuilder =
                new ModelBuilderImpl<>(
                        KogitoPackageSources::dumpSources,
                        KogitoKnowledgeBuilderConfigurationImpl.fromContext(context()),
                        dummyReleaseId,
                        hotReloadMode);

        CompositeKnowledgeBuilder batch = modelBuilder.batch();
        resources.forEach(f -> addResource(batch, f));

        try {
            batch.build();
        } catch (RuntimeException e) {
            for (DroolsError error : modelBuilder.getErrors().getErrors()) {
                LOGGER.error(error.toString());
            }
            LOGGER.error(e.getMessage());
            throw new RuleCodegenError(e, modelBuilder.getErrors().getErrors());
        }

        if (modelBuilder.hasErrors()) {
            for (DroolsError error : modelBuilder.getErrors().getErrors()) {
                LOGGER.error(error.toString());
            }
            throw new RuleCodegenError(modelBuilder.getErrors().getErrors());
        }

        List<GeneratedFile> generatedFiles = new ArrayList<>(generateModels(modelBuilder));

        List<DroolsError> errors = new ArrayList<>();
        boolean hasRuleUnits = !ruleUnitGenerators.isEmpty();

        if (hasRuleUnits) {
            generateRuleUnits(errors, generatedFiles);
            generateRuleUnitREST(generatedFiles);

        } else if (context().hasClassAvailable("org.kie.kogito.legacy.rules.KieRuntimeBuilder")) {
            ModelSourceClass modelSourceClass = kieModuleThing.createModelSourceClass(dummyReleaseId, modelBuilder);
            ProjectRuntimeGenerator projectRuntimeGenerator = kieModuleThing.createProjectRuntimeGenerator(modelSourceClass);

            generatedFiles.add(new GeneratedFile(RuleCodegen.RULE_TYPE, modelSourceClass.getName(), modelSourceClass.generate()));
            generatedFiles.add(new GeneratedFile(RuleCodegen.RULE_TYPE, projectRuntimeGenerator.getName(), projectRuntimeGenerator.generate()));

        } else if (hasRuleFiles()) { // this additional check is necessary because also properties or java files can be loaded
            throw new IllegalStateException("Found DRL files using legacy API, add org.kie.kogito:kogito-legacy-api dependency to enable it");
        }

        if (!errors.isEmpty()) {
            throw new RuleCodegenError(errors);
        }

        return generatedFiles;
    }

    private void addResource(CompositeKnowledgeBuilder batch, Resource resource) {
        if (resource.getResourceType() == ResourceType.PROPERTIES) {
            return;
        }
        if (resource.getResourceType() == ResourceType.DTABLE) {
            Resource resourceProps = findPropertiesResource(resource);
            if (resourceProps != null) {
                ResourceConfiguration conf = loadResourceConfiguration(resource.getSourcePath(), x -> true, x -> {
                    try {
                        return resourceProps.getInputStream();
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                });
                if (conf instanceof DecisionTableConfiguration) {
                    addDTableToCompiler(batch, resource, ((DecisionTableConfiguration) conf));
                    return;
                }
            }
        }
        batch.add(resource, resource.getResourceType());
    }

    private Resource findPropertiesResource(Resource resource) {
        return resources.stream().filter(r -> r.getSourcePath().equals(resource.getSourcePath() + ".properties")).findFirst().orElse(null);
    }

    private List<GeneratedFile> generateModels(ModelBuilderImpl<KogitoPackageSources> modelBuilder) {
        List<GeneratedFile> modelFiles = new ArrayList<>();
        List<org.drools.modelcompiler.builder.GeneratedFile> legacyModelFiles = new ArrayList<>();

        for (KogitoPackageSources pkgSources : modelBuilder.getPackageSources()) {
            pkgSources.collectGeneratedFiles(legacyModelFiles);

            org.drools.modelcompiler.builder.GeneratedFile reflectConfigSource = pkgSources.getReflectConfigSource();
            if (reflectConfigSource != null) {
                modelFiles.add(new GeneratedFile(GeneratedFileType.INTERNAL_RESOURCE,
                        reflectConfigSource.getPath(),
                        reflectConfigSource.getData()));
            }

            Collection<RuleUnitDescription> ruleUnits = pkgSources.getRuleUnits();
            for (RuleUnitDescription ruleUnit : ruleUnits) {
                String canonicalName = ruleUnit.getCanonicalName();
                RuleUnitGenerator ruSource = new RuleUnitGenerator(context(), ruleUnit, pkgSources.getRulesFileName())
                        .withQueries(pkgSources.getQueriesInRuleUnit(canonicalName))
                        .mergeConfig(configs.get(canonicalName));

                ruleUnitGenerators.add(ruSource);

                // merge config from the descriptor with configs from application.conf
                // application.conf overrides any other config
                org.drools.ruleunits.api.RuleUnitConfig config =
                        ((AbstractRuleUnitDescription) ruleUnit).getConfig()
                                .merged(configs.get(ruleUnit.getCanonicalName()));

                // only Class<?> has config for now
                kieModuleThing.addRuleUnitConfig(ruleUnit, config);
            }
        }

        modelFiles.addAll(convertGeneratedRuleFile(legacyModelFiles));
        return modelFiles;
    }

    private Collection<GeneratedFile> convertGeneratedRuleFile(Collection<org.drools.modelcompiler.builder.GeneratedFile> legacyModelFiles) {
        return legacyModelFiles.stream().map(f -> new GeneratedFile(
                RuleCodegen.RULE_TYPE,
                f.getPath(), f.getData()))
                .collect(toList());
    }

    private void generateRuleUnits(List<DroolsError> errors, List<GeneratedFile> generatedFiles) {
        RuleUnitHelper ruleUnitHelper = new RuleUnitHelper(context().getClassLoader(), hotReloadMode);

        for (RuleUnitGenerator ruleUnit : ruleUnitGenerators) {
            ruleUnitHelper.initRuleUnitHelper(ruleUnit.getRuleUnitDescription());

            List<String> queryClasses = generateQueriesEndpoint(errors, generatedFiles, ruleUnitHelper, ruleUnit);

            generatedFiles.add(ruleUnit.generate());

            RuleUnitInstanceGenerator ruleUnitInstance = ruleUnit.instance(ruleUnitHelper, queryClasses);
            generatedFiles.add(ruleUnitInstance.generate());

            ruleUnit.pojo(ruleUnitHelper).ifPresent(p -> generatedFiles.add(p.generate()));

            queryEventDriven(generatedFiles, ruleUnit);
        }
    }

    private void queryEventDriven(List<GeneratedFile> generatedFiles, RuleUnitGenerator ruleUnit) {
        if (context().getAddonsConfig().useEventDrivenRules()) {
            ruleUnit.queryEventDrivenExecutors().stream()
                    .map(QueryEventDrivenExecutorGenerator::generate)
                    .forEach(generatedFiles::add);
        }
    }

    private void generateRuleUnitREST(List<GeneratedFile> generatedFiles) {
        if (context().hasRESTForGenerator(this)) {
            TemplatedGenerator generator = TemplatedGenerator.builder()
                    .withTemplateBasePath(TEMPLATE_RULE_FOLDER)
                    .build(context(), "KogitoObjectMapper");

            generatedFiles.add(new GeneratedFile(REST_TYPE,
                    generator.generatedFilePath(),
                    generator.compilationUnitOrThrow().toString()));
        }
    }

    private List<String> generateQueriesEndpoint(List<DroolsError> errors, List<GeneratedFile> generatedFiles, RuleUnitHelper ruleUnitHelper, RuleUnitGenerator ruleUnit) {

        List<QueryEndpointGenerator> queries = ruleUnit.queries();
        if (queries.isEmpty()) {
            return Collections.emptyList();
        }

        if (!context().hasDI()) {
            generatedFiles.add(new RuleUnitDTOSourceClass(ruleUnit.getRuleUnitDescription(), ruleUnitHelper).generate());
        }
        Optional<String> domainDashboard = GrafanaConfigurationWriter.generateDomainSpecificDrlDashboard(
                domainDashboardDrlTemplate,
                ruleUnit.typeName(),
                context().getPropertiesMap(),
                ruleUnit.typeName(),
                context().getGAV().orElse(KogitoGAV.EMPTY_GAV),
                context().getAddonsConfig().useTracing());
        String dashboardName = GrafanaConfigurationWriter.buildDashboardName(context().getGAV(), ruleUnit.typeName());
        domainDashboard.ifPresent(dashboard -> generatedFiles.addAll(DashboardGeneratedFileUtils.domain(dashboard, dashboardName + ".json")));
        return queries.stream().map(q -> generateQueryEndpoint(errors, generatedFiles, q))
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty)).collect(toList());
    }

    private Optional<String> generateQueryEndpoint(List<DroolsError> errors, List<GeneratedFile> generatedFiles, QueryEndpointGenerator query) {
        if (!query.validate()) {
            errors.add(query.getError());
            return Optional.empty();
        }
        if (context().hasRESTForGenerator(this)) {
            if (context().getAddonsConfig().usePrometheusMonitoring()) {
                String dashboardName = GrafanaConfigurationWriter.buildDashboardName(context().getGAV(), query.getEndpointName());
                Optional<String> operationalDashboard = GrafanaConfigurationWriter.generateOperationalDashboard(
                        operationalDashboardDrlTemplate,
                        query.getEndpointName(),
                        context().getPropertiesMap(),
                        query.getEndpointName(),
                        context().getGAV().orElse(KogitoGAV.EMPTY_GAV),
                        context().getAddonsConfig().useTracing());
                operationalDashboard.ifPresent(dashboard -> generatedFiles.addAll(DashboardGeneratedFileUtils.operational(dashboard, dashboardName + ".json")));
            }
            generatedFiles.add(query.generate());
        }

        QueryGenerator queryGenerator = query.getQueryGenerator();
        generatedFiles.add(queryGenerator.generate());
        return Optional.of(queryGenerator.getQueryClassName());
    }


    public RuleCodegen withHotReloadMode() {
        this.hotReloadMode = true;
        return this;
    }

    private boolean hasRuleFiles() {
        return resources.stream().anyMatch(RuleCodegen::isRuleFile);
    }

    private static boolean isRuleFile(Resource resource) {
        return resource.getResourceType() == ResourceType.DRL || resource.getResourceType() == ResourceType.DTABLE;
    }

    @Override
    public int priority() {
        return 20;
    }
}
