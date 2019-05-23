package org.kie.submarine.codegen;

import java.util.Collection;
import java.util.Map;

import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class GeneratorInterfaceTest {

    @Test
    public void getLabels() {
        Assertions.assertThat(getAnonymousGeneratorInstance().getLabels()).isEmpty();
    }

    private Generator getAnonymousGeneratorInstance() {
        return new Generator() {
            @Override
            public Collection<MethodDeclaration> factoryMethods() {
                return null;
            }

            @Override
            public Collection<BodyDeclaration<?>> applicationBodyDeclaration() {
                return null;
            }

            @Override
            public Collection<GeneratedFile> generate() {
                return null;
            }

            @Override
            public void updateConfig(ConfigGenerator cfg) {

            }

            @Override
            public void setPackageName(String packageName) {

            }

            @Override
            public void setDependencyInjection(boolean dependencyInjection) {

            }
        };
    }
}