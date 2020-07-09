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
import java.util.Properties;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.GeneratorContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PredictionCodegenTest {

    @Test
    public void generateAllFiles() throws Exception {

        GeneratorContext context = stronglyTypedContext();

        PredictionCodegen codeGenerator = PredictionCodegen.ofPath(Paths.get("src/test/resources/prediction/test_regression.pmml").toAbsolutePath());
        codeGenerator.setContext(context);

//        List<GeneratedFile> generatedFiles = codeGenerator.generate();
//        assertEquals(5, generatedFiles.size());

//        assertIterableEquals(Arrays.asList(
//                "decision/InputSet.java",
//                "decision/TEmployee.java",
//                "decision/TAddress.java",
//                "decision/TPayroll.java",
//                "decision/VacationsResource.java"
//                             ),
//                             fileNames(generatedFiles)
//        );

        ClassOrInterfaceDeclaration classDeclaration = codeGenerator.moduleGenerator().classDeclaration();
        assertNotNull(classDeclaration);
    }

    private GeneratorContext stronglyTypedContext() {
        Properties properties = new Properties();
        properties.put(PredictionCodegen.STRONGLY_TYPED_CONFIGURATION_KEY, Boolean.TRUE.toString());
        return GeneratorContext.ofProperties(properties);
    }



}
