package org.kie.submarine.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class ApplicationGeneratorTest {

    private static final String PACKAGE_NAME = "org.drools.test";
    private static final String EXPECTED_APPLICATION_NAME = PACKAGE_NAME + ".Application";

    @Test
    public void targetCanonicalName() {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, new File(""));
        Assertions.assertThat(appGenerator.targetCanonicalName()).isEqualTo(EXPECTED_APPLICATION_NAME);
    }

    @Test
    public void generatedFilePath() {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, new File(""));
        Assertions.assertThat(appGenerator.generatedFilePath()).isEqualTo(EXPECTED_APPLICATION_NAME.replace(".", "/") + ".java");
    }

    @Test
    public void addFactoryMethods() {
    }

    @Test
    public void compilationUnit() {
    }

    @Test
    public void withDependencyInjection() {
        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, new File(""));
        Assertions.assertThat(appGenerator.withDependencyInjection(false)).isSameAs(appGenerator);

        // TODO - check that dep. injection is not set

        // TODO - check that dep. injection is set
    }

    @Test
    public void generate() {
    }

    @Test
    public void withGenerator() {
    }

    @Test
    public void writeLabelsImageMetadata() throws IOException {
//        final Path tempDirectory = Files.createTempDirectory("wlim");
//        final ApplicationGenerator appGenerator = new ApplicationGenerator(PACKAGE_NAME, tempDirectory.toFile());
//
//        final Map<String, String> labels = new HashMap<>();
//        labels.put("testKey1", "testValue1");
//        labels.put("testKey2", "testValue2");
//        labels.put("testKey3", "testValue3");
//
//        appGenerator.writeLabelsImageMetadata(labels);

        // TODO test file exists and contains the labels and values
    }
}