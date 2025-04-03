package org.kie.kogito.codegen.manager.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.kogito.codegen.api.Generator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.decision.DecisionCodegen;
import org.kie.kogito.codegen.prediction.PredictionCodegen;
import org.kie.kogito.codegen.process.ProcessCodegen;
import org.kie.kogito.codegen.process.persistence.PersistenceGenerator;
import org.kie.kogito.codegen.rules.RuleCodegen;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class CodeGenManagerUtilTest {

    private static final List<String> generatorNames;

    static {
        generatorNames = new ArrayList<>();
        generatorNames.add(RuleCodegen.GENERATOR_NAME);
        generatorNames.add(ProcessCodegen.GENERATOR_NAME);
        generatorNames.add(PredictionCodegen.GENERATOR_NAME);
        generatorNames.add(DecisionCodegen.GENERATOR_NAME);
        generatorNames.add(PersistenceGenerator.GENERATOR_NAME);
    }

    @ParameterizedTest
    @MethodSource("getGeneratorNamesStream")
    void overwritePropertiesIfNeededWithNull(String generatorName) {
        String expectedWrittenProperty = Generator.CONFIG_PREFIX + generatorName;
        KogitoBuildContext kogitoBuildContextMocked = Mockito.mock(KogitoBuildContext.class);
        CodeGenManagerUtil.ProjectParameters parameters = new CodeGenManagerUtil.ProjectParameters(CodeGenManagerUtil.Framework.QUARKUS,
                null,
                null,
                null,
                null,
                false);
        CodeGenManagerUtil.overwritePropertiesIfNeeded(kogitoBuildContextMocked, parameters);
        if (generatorName.equals(PersistenceGenerator.GENERATOR_NAME)) {
            Mockito.verify(kogitoBuildContextMocked, Mockito.times(1)).setApplicationProperty(expectedWrittenProperty, "false"); // being a boolean property, it default to false
        } else {
            Mockito.verify(kogitoBuildContextMocked, Mockito.never()).setApplicationProperty(Mockito.eq(expectedWrittenProperty), Mockito.any());
        }
    }

    @ParameterizedTest
    @MethodSource("getGeneratorNamesStream")
    void overwritePropertyIfNeededWithNotNull(String generatorName) {
        String propertyValue = "notnull";
        String expectedWrittenProperty = Generator.CONFIG_PREFIX + generatorName;
        KogitoBuildContext kogitoBuildContextMocked = Mockito.mock(KogitoBuildContext.class);
        CodeGenManagerUtil.overwritePropertyIfNeeded(kogitoBuildContextMocked, generatorName, propertyValue);
        Mockito.verify(kogitoBuildContextMocked, Mockito.times(1)).setApplicationProperty(expectedWrittenProperty, propertyValue);
    }

    @ParameterizedTest
    @MethodSource("getGeneratorNamesStream")
    void overwritePropertyIfNeededWithEmpty(String generatorName) {
        String propertyValue = "";
        String expectedWrittenProperty = Generator.CONFIG_PREFIX + generatorName;
        KogitoBuildContext kogitoBuildContextMocked = Mockito.mock(KogitoBuildContext.class);
        CodeGenManagerUtil.overwritePropertyIfNeeded(kogitoBuildContextMocked, generatorName, propertyValue);
        Mockito.verify(kogitoBuildContextMocked, Mockito.never()).setApplicationProperty(expectedWrittenProperty, propertyValue);
    }

    @ParameterizedTest
    @MethodSource("getGeneratorNamesStream")
    void overwritePropertyIfNeededWithNull(String generatorName) {
        String propertyValue = null;
        String expectedWrittenProperty = Generator.CONFIG_PREFIX + generatorName;
        KogitoBuildContext kogitoBuildContextMocked = Mockito.mock(KogitoBuildContext.class);
        CodeGenManagerUtil.overwritePropertyIfNeeded(kogitoBuildContextMocked, generatorName, propertyValue);
        Mockito.verify(kogitoBuildContextMocked, Mockito.never()).setApplicationProperty(expectedWrittenProperty, propertyValue);
    }

    static Stream<String> getGeneratorNamesStream() {
        return generatorNames.stream();
    }

    static CodeGenManagerUtil.ProjectParameters getProjectParameters() {
        return new CodeGenManagerUtil.ProjectParameters(CodeGenManagerUtil.Framework.QUARKUS,
                "false",
                "false",
                "false",
                "false",
                false);
    }
}
