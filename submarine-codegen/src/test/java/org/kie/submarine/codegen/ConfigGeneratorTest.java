package org.kie.submarine.codegen;

import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.submarine.StaticConfig;
import org.kie.submarine.codegen.process.config.ProcessConfigGenerator;
import org.mockito.Mockito;

public class ConfigGeneratorTest {

    @Test
    public void withProcessConfig() {
        final ConfigGenerator generator = new ConfigGenerator();
        final ProcessConfigGenerator processConfigGenerator = Mockito.mock(ProcessConfigGenerator.class);
        Assertions.assertThat(generator.withProcessConfig(processConfigGenerator)).isSameAs(generator);
    }

    @Test
    public void withProcessConfigNull() {
        final ConfigGenerator generator = new ConfigGenerator();
        Assertions.assertThat(generator.withProcessConfig(null)).isSameAs(generator);
    }

    @Test
    public void newInstanceNoProcessConfig() {
        newInstance(null, NullLiteralExpr.class);
    }

    @Test
    public void newInstanceWithProcessConfig() {
        final ProcessConfigGenerator processConfigGenerator = Mockito.mock(ProcessConfigGenerator.class);
        Mockito.when(processConfigGenerator.newInstance()).thenReturn(new ObjectCreationExpr());
        newInstance(processConfigGenerator, ObjectCreationExpr.class);
    }

    private void newInstance(final ProcessConfigGenerator processConfigGenerator, final Class expectedArgumentType) {
        ObjectCreationExpr expression = new ConfigGenerator().withProcessConfig(processConfigGenerator).newInstance();
        Assertions.assertThat(expression.getType().asString()).isEqualTo(StaticConfig.class.getCanonicalName());
        Assertions.assertThat(expression.getArguments()).hasSize(1);
        Assertions.assertThat(expression.getArgument(0)).isInstanceOf(expectedArgumentType);
    }
}