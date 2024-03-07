package org.kie.kogito.codegen.decision;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.drools.io.FileSystemResource;
import org.drools.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.kie.api.io.Resource;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.kogito.dmn.DMNKogito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenUtilsTest {

    @Test
    void getDefinitionsFileFromModelWithSpace() {
        File dmnFile = FileUtils.getFile("Traffic Violation.dmn");
        assertNotNull(dmnFile);
        assertTrue(dmnFile.exists());
        Resource dmnResource = new FileSystemResource(dmnFile, StandardCharsets.UTF_8.name());

        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults()
                .setRootClassLoader(Thread.currentThread().getContextClassLoader())
                .buildConfiguration()
                .fromResources(Collections.singleton(dmnResource))
                .getOrElseThrow(e -> new RuntimeException("Error compiling DMN model(s)", e));
        assertThat(dmnRuntime.getModels()).hasSize(1);
        final DMNModel dmnModel = dmnRuntime.getModel("https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF", "Traffic Violation Model Name");
        assertNotNull(dmnModel);
        String expected = "Traffic_Violation.json";
        assertEquals(expected, CodegenUtils.getDefinitionsFileFromModel(dmnModel));
    }

    @Test
    void geNameForDefinitionsFileWithSourcePath() {
        File dmnFile = FileUtils.getFile("Traffic Violation.dmn");
        assertNotNull(dmnFile);
        assertTrue(dmnFile.exists());
        Resource dmnResource = new FileSystemResource(dmnFile, StandardCharsets.UTF_8.name());
        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults()
                .setRootClassLoader(Thread.currentThread().getContextClassLoader())
                .buildConfiguration()
                .fromResources(Collections.singleton(dmnResource))
                .getOrElseThrow(e -> new RuntimeException("Error compiling DMN model(s)", e));
        assertThat(dmnRuntime.getModels()).hasSize(1);
        final DMNModel dmnModel = dmnRuntime.getModel("https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF", "Traffic Violation Model Name");
        assertNotNull(dmnModel);
        String expected = "Traffic Violation.dmn";
        assertEquals(expected, CodegenUtils.geNameForDefinitionsFile(dmnModel));
    }

    @Test
    void geNameForDefinitionsFileWithoutSourcePath() throws FileNotFoundException {
        File dmnFile = FileUtils.getFile("Traffic Violation.dmn");
        assertNotNull(dmnFile);
        assertTrue(dmnFile.exists());
        DMNRuntime dmnRuntime = DMNKogito.createGenericDMNRuntime(new FileReader(dmnFile));
        assertNotNull(dmnRuntime);
        assertThat(dmnRuntime.getModels()).hasSize(1);
        final DMNModel dmnModel = dmnRuntime.getModel("https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF", "Traffic Violation Model Name");
        assertNotNull(dmnModel);
        String expected = "Traffic Violation Model Name.dmn";
        assertEquals(expected, CodegenUtils.geNameForDefinitionsFile(dmnModel));
    }
}
