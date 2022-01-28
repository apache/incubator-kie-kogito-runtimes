package org.kie.kogito.codegen.rules;

import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.modelcompiler.builder.ModelBuilderImpl;
import org.drools.modelcompiler.builder.ModelSourceClass;
import org.drools.ruleunits.api.conf.ClockType;
import org.drools.ruleunits.api.conf.EventProcessingType;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.conf.SessionsPoolOption;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.util.maven.support.ReleaseIdImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.drools.compiler.kie.builder.impl.KieBuilderImpl.setDefaultsforEmptyKieModule;

public class KieModuleThing {
    private KieModuleModel kieModuleModel;
    private final KogitoBuildContext context;

    public KieModuleThing(KogitoBuildContext context, KieModuleModel kieModuleModel) {
        this.context = context;
        this.kieModuleModel = kieModuleModel;
        setDefaultsforEmptyKieModule(kieModuleModel);
    }

    static KieModuleThing fromContext(KogitoBuildContext context) {
        return new KieModuleThing(context, lookupKieModuleModel(context.getAppPaths().getResourcePaths()));
    }


    private static KieModuleModel lookupKieModuleModel(Path[] resourcePaths) {
        for (Path resourcePath : resourcePaths) {
            Path moduleXmlPath = resourcePath.resolve(KieModuleModelImpl.KMODULE_JAR_PATH.asString());
            if (Files.exists(moduleXmlPath)) {
                try (ByteArrayInputStream bais = new ByteArrayInputStream(Files.readAllBytes(moduleXmlPath))) {
                    return KieModuleModelImpl.fromXML(bais);
                } catch (IOException e) {
                    throw new UncheckedIOException("Impossible to open " + moduleXmlPath, e);
                }
            }
        }

        return new KieModuleModelImpl();
    }


    void generateProject(ReleaseIdImpl dummyReleaseId, ModelBuilderImpl<KogitoPackageSources> modelBuilder, List<GeneratedFile> generatedFiles) {
        ModelSourceClass modelSourceClass = createModelSourceClass(dummyReleaseId, modelBuilder);

        ProjectRuntimeGenerator projectRuntimeGenerator = createProjectRuntimeGenerator(modelSourceClass);
    }

    public ProjectRuntimeGenerator createProjectRuntimeGenerator(ModelSourceClass modelSourceClass) {
        return new ProjectRuntimeGenerator(modelSourceClass.getModelMethod(), context);
    }

    public ModelSourceClass createModelSourceClass(ReleaseIdImpl dummyReleaseId, ModelBuilderImpl<KogitoPackageSources> modelBuilder) {
        ModelSourceClass modelSourceClass = new ModelSourceClass(dummyReleaseId, kieModuleModel.getKieBaseModels(), getModelByKBase(modelBuilder));
        return modelSourceClass;
    }

    private Map<String, List<String>> getModelByKBase(ModelBuilderImpl<KogitoPackageSources> modelBuilder) {
        Map<String, String> modelsByPackage = getModelsByPackage(modelBuilder);
        Map<String, List<String>> modelsByKBase = new HashMap<>();
        for (Map.Entry<String, KieBaseModel> entry : kieModuleModel.getKieBaseModels().entrySet()) {
            List<String> kieBasePackages = entry.getValue().getPackages();
            boolean isAllPackages = kieBasePackages.isEmpty() || (kieBasePackages.size() == 1 && kieBasePackages.get(0).equals("*"));
            modelsByKBase.put(entry.getKey(),
                    isAllPackages ? new ArrayList<>(modelsByPackage.values()) : kieBasePackages.stream().map(modelsByPackage::get).filter(Objects::nonNull).collect(toList()));
        }
        return modelsByKBase;
    }


    private String ruleUnit2KieBaseName(String ruleUnit) {
        return ruleUnit.replace('.', '$') + "KieBase";
    }


    private String ruleUnit2KieSessionName(String ruleUnit) {
        return ruleUnit.replace('.', '$') + "KieSession";
    }

    private Map<String, String> getModelsByPackage(ModelBuilderImpl<KogitoPackageSources> modelBuilder) {
        Map<String, String> modelsByPackage = new HashMap<>();
        for (KogitoPackageSources pkgSources : modelBuilder.getPackageSources()) {
            modelsByPackage.put(pkgSources.getPackageName(), pkgSources.getPackageName() + "." + pkgSources.getRulesFileName());
        }
        return modelsByPackage;
    }


    void addRuleUnitConfig(RuleUnitDescription ruleUnitDescription, org.drools.ruleunits.api.RuleUnitConfig config) {
        KieBaseModel unitKieBaseModel = kieModuleModel.newKieBaseModel(ruleUnit2KieBaseName(ruleUnitDescription.getCanonicalName()));
        unitKieBaseModel.setEventProcessingMode(org.kie.api.conf.EventProcessingOption.CLOUD);
        unitKieBaseModel.addPackage(ruleUnitDescription.getPackageName());


        OptionalInt sessionsPool = config.getSessionPool();
        if (sessionsPool.isPresent()) {
            unitKieBaseModel.setSessionsPool(SessionsPoolOption.get(sessionsPool.getAsInt()));
        }
        EventProcessingType eventProcessingType = config.getDefaultedEventProcessingType();
        if (eventProcessingType == EventProcessingType.STREAM) {
            unitKieBaseModel.setEventProcessingMode(EventProcessingOption.STREAM);
        }

        KieSessionModel unitKieSessionModel = unitKieBaseModel.newKieSessionModel(ruleUnit2KieSessionName(ruleUnitDescription.getCanonicalName()));
        unitKieSessionModel.setType(KieSessionModel.KieSessionType.STATEFUL);
        ClockType clockType = config.getDefaultedClockType();
        if (clockType == ClockType.PSEUDO) {
            unitKieSessionModel.setClockType(ClockTypeOption.PSEUDO);
        }
    }

}
