package org.kie.submarine.codegen;

import java.nio.charset.StandardCharsets;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneratedFileTest {

    private static final GeneratedFile.Type TEST_TYPE = GeneratedFile.Type.RULE;
    private static final String TEST_RELATIVE_PATH = "relativePath";
    private static final byte[] TEST_CONTENTS = "testContents".getBytes(StandardCharsets.UTF_8);

    private static GeneratedFile testFile;

    @BeforeClass
    public static void createTestFile() {
        testFile = new GeneratedFile(TEST_TYPE, TEST_RELATIVE_PATH, TEST_CONTENTS);
    }

    @Test
    public void relativePath() {
        Assertions.assertThat(testFile.relativePath()).isEqualTo(TEST_RELATIVE_PATH);
    }

    @Test
    public void contents() {
        Assertions.assertThat(testFile.contents()).isEqualTo(TEST_CONTENTS);
    }

    @Test
    public void getType() {
        Assertions.assertThat(testFile.getType()).isEqualTo(TEST_TYPE);
    }
}