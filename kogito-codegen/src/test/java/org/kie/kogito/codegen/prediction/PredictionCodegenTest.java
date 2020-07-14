/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.prediction;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.codegen.AbstractCodegenTest;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.GeneratorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PredictionCodegenTest extends AbstractCodegenTest {

    @Test
    public void generateAllFiles() throws Exception {

        GeneratorContext context = stronglyTypedContext();

        PredictionCodegen codeGenerator = PredictionCodegen.ofPath(Paths.get("src/test/resources/prediction/test_regression.pmml").toAbsolutePath());
        codeGenerator.setContext(context);

        List<GeneratedFile> generatedFiles = codeGenerator.generate();
        assertEquals(3, generatedFiles.size());

        ClassOrInterfaceDeclaration classDeclaration = codeGenerator.moduleGenerator().classDeclaration();
        assertNotNull(classDeclaration);
    }

    @Test
    public void generateCode() throws Exception {
        Map<TYPE, List<String>> resourcesTypeMap = new HashMap<>();
        resourcesTypeMap.put(TYPE.PREDICTION, Collections.singletonList("prediction/test_regression.pmml"));
        Application app = generateCode(resourcesTypeMap, false);
        assertThat(app).isNotNull();
    }

    private GeneratorContext stronglyTypedContext() {
        Properties properties = new Properties();
        properties.put(PredictionCodegen.STRONGLY_TYPED_CONFIGURATION_KEY, Boolean.TRUE.toString());
        return GeneratorContext.ofProperties(properties);
    }



}
