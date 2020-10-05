/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.codegen.process.events;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.process.ProcessGenerationUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

// TODO: add more tests

class TopicsInformationResourceGeneratorTest {

    @Test
    void verifyProcessWithMessageEvent() {
        final TopicsInformationResourceGenerator generator =
                new TopicsInformationResourceGenerator(ProcessGenerationUtils.execModelFromProcessFile("/messageevent/IntermediateCatchEventMessage.bpmn2"));
        final String source = generator.generate();
        assertThat(source).isNotNull();
        assertThat(generator.getTriggers()).hasSize(1);

        final ClassOrInterfaceDeclaration clazz = StaticJavaParser
                .parse(source)
                .getClassByName(generator.getClassName())
                .orElseThrow(() -> new IllegalArgumentException("Class does not exists"));

        assertThat(clazz).isNotNull();
        /*
        assertThat(clazz.getFields().stream()
                           .filter(f -> f.get .getAnnotationByName("Channel").isPresent())
                           .count()).isEqualTo(1L);
        assertThat(clazz.getFields().stream()
                           .filter(f -> f.getAnnotationByName("Inject").isPresent())
                           .count()).isEqualTo(2L);
         */
    }

}