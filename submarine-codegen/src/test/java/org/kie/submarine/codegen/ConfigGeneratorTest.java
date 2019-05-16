package org.kie.submarine.codegen;

import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import org.junit.Test;
import org.kie.submarine.StaticConfig;
import org.kie.submarine.codegen.process.config.ProcessConfigGenerator;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigGeneratorTest {

    @Test
    public void withProcessConfig() {
        final ConfigGenerator generator = new ConfigGenerator();
        final ProcessConfigGenerator processConfigGenerator = Mockito.mock(ProcessConfigGenerator.class);
        final ConfigGenerator returnedConfigGenerator = generator.withProcessConfig(processConfigGenerator);
        assertThat(returnedConfigGenerator).isNotNull();
        assertThat(returnedConfigGenerator).isSameAs(generator);
    }

    @Test
    public void withProcessConfigNull() {
        final ConfigGenerator generator = new ConfigGenerator();
        final ConfigGenerator returnedConfigGenerator = generator.withProcessConfig(null);
        assertThat(returnedConfigGenerator).isNotNull();
        assertThat(returnedConfigGenerator).isSameAs(generator);
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
        assertThat(expression).isNotNull();

        assertThat(expression.getType()).isNotNull();
        assertThat(expression.getType().asString()).isEqualTo(StaticConfig.class.getCanonicalName());

        assertThat(expression.getArguments()).isNotNull();
        assertThat(expression.getArguments()).hasSize(1);
        assertThat(expression.getArgument(0)).isInstanceOf(expectedArgumentType);
    }
}