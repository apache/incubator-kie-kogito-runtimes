package org.kie.submarine.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationGeneratorTest {

    private static final String PACKAGE_NAME = "org.drools.test";
    private static final String EXPECTED_APPLICATION_NAME = PACKAGE_NAME + ".Application";

    @Test
    public void targetCanonicalName() {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, new File(""));
        assertThat(appGenerator.targetCanonicalName()).isNotNull();
        assertThat(appGenerator.targetCanonicalName()).isEqualTo(EXPECTED_APPLICATION_NAME);
    }

    @Test
    public void generatedFilePath() {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, new File(""));
        assertThat(appGenerator.generatedFilePath()).isNotNull();
        assertThat(appGenerator.generatedFilePath()).isEqualTo(EXPECTED_APPLICATION_NAME.replace(".", "/") + ".java");
    }

    @Test
    public void compilationUnit() {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, new File("target"));
        assertCompilationUnit(appGenerator.compilationUnit(), false, 0);
    }

    @Test
    public void compilationUnitWithCDI() {
        final ApplicationGenerator initialAppGenerator = new ApplicationGenerator(PACKAGE_NAME, new File("target"));
        final ApplicationGenerator appGenerator = initialAppGenerator.withDependencyInjection(true);
        assertThat(appGenerator).isSameAs(initialAppGenerator);
        assertCompilationUnit(appGenerator.compilationUnit(), true, 0);
    }

    @Test
    public void compilationUnitWithFactoryMethods() {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, new File("target"));
        final String testMethodName = "testMethod";
        final MethodDeclaration methodDeclaration = new MethodDeclaration();
        methodDeclaration.setName(testMethodName);

        appGenerator.addFactoryMethods(Collections.singleton(methodDeclaration));

        final CompilationUnit compilationUnit = appGenerator.compilationUnit();
        assertCompilationUnit(compilationUnit, false, 1);

        final TypeDeclaration mainAppClass = compilationUnit.getTypes().get(0);
        assertThat(mainAppClass.getMembers()
                           .stream()
                           .filter(member -> member instanceof MethodDeclaration
                                   && ((MethodDeclaration) member).getName().toString().equals(testMethodName)))
                .hasSize(1);
    }

    @Test
    public void generate() {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, new File("target"));
        final Collection<GeneratedFile> generatedFiles = appGenerator.generate();
        assertThat(generatedFiles).isNotNull();
        assertThat(generatedFiles).hasSize(1);

        final GeneratedFile generatedFile = generatedFiles.iterator().next();
        assertThat(generatedFile).isNotNull();
        assertThat(generatedFile.getType()).isEqualTo(GeneratedFile.Type.APPLICATION);
        assertThat(generatedFile.relativePath()).isEqualTo(EXPECTED_APPLICATION_NAME.replace(".", "/") + ".java");
        assertThat(generatedFile.contents()).isEqualTo(appGenerator.compilationUnit().toString().getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void generateWithOtherGenerator() {
    }

    @Test
    public void writeLabelsImageMetadata() throws IOException {
        final Path targetDirectory = Paths.get("target");
        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, targetDirectory.toFile());

        final Map<String, String> labels = new HashMap<>();
        labels.put("testKey1", "testValue1");
        labels.put("testKey2", "testValue2");
        labels.put("testKey3", "testValue3");

        appGenerator.writeLabelsImageMetadata(labels);

        try (Stream<Path> stream = Files.walk(targetDirectory, 1)) {
            final Optional<Path> generatedFile = stream
                    .filter(file -> file.getFileName().toString().equals("image_metadata.json"))
                    .findFirst();
            assertThat(generatedFile).isPresent();

            ObjectMapper mapper = new ObjectMapper();
            final Map<String, List> elementsFromFile = mapper.readValue(generatedFile.get().toFile(),
                                                                        new TypeReference<Map<String, List>>(){});
            assertThat(elementsFromFile).hasSize(1);
            final List<Map<String, String>> listWithLabelsMap = elementsFromFile.entrySet().iterator().next().getValue();
            assertThat(listWithLabelsMap).isNotNull();
            assertThat(listWithLabelsMap).hasSize(1);
            assertThat(listWithLabelsMap.get(0)).containsAllEntriesOf(labels);
        }
    }

    private void assertCompilationUnit(final CompilationUnit compilationUnit, final boolean checkCDI, final int expectedNumberOfFactoryMethods) {
        assertThat(compilationUnit).isNotNull();

        assertThat(compilationUnit.getPackageDeclaration()).isPresent();
        assertThat(compilationUnit.getPackageDeclaration().get().getName().toString()).isEqualTo(PACKAGE_NAME);

        assertThat(compilationUnit.getImports()).isNotNull();
        assertThat(compilationUnit.getImports()).hasSize(1);
        assertThat(compilationUnit.getImports().get(0).getName().toString()).isEqualTo("org.kie.submarine.Config");

        assertThat(compilationUnit.getTypes()).isNotNull();
        assertThat(compilationUnit.getTypes()).hasSize(1);

        final TypeDeclaration mainAppClass = compilationUnit.getTypes().get(0);
        assertThat(mainAppClass).isNotNull();
        assertThat(mainAppClass.getName().toString()).isEqualTo("Application");

        if (checkCDI) {
            assertThat(mainAppClass.getAnnotations()).isNotEmpty();
            assertThat(mainAppClass.getAnnotationByName("Singleton")).isPresent();
        } else {
            assertThat(mainAppClass.getAnnotationByName("Singleton")).isNotPresent();
        }

        assertThat(mainAppClass.getMembers()).isNotNull();
        assertThat(mainAppClass.getMembers()).hasSize(2 + expectedNumberOfFactoryMethods);

        assertThat(mainAppClass.getMembers()
                           .stream()
                           .filter(member -> member instanceof MethodDeclaration
                                   && ((MethodDeclaration) member).getName().toString().equals("config")
                                   && !((MethodDeclaration) member).isStatic()))
                .hasSize(1);
        assertThat(mainAppClass.getMembers()
                           .stream()
                           .filter(member -> member instanceof FieldDeclaration
                                   && ((FieldDeclaration) member).getVariable(0).getName().toString().equals("config")
                                   && ((FieldDeclaration) member).isStatic()))
                .hasSize(1);

        assertThat(mainAppClass.getMember(0)).isInstanceOfAny(MethodDeclaration.class, FieldDeclaration.class);
        assertThat(mainAppClass.getMember(1)).isInstanceOfAny(MethodDeclaration.class, FieldDeclaration.class);
    }
}