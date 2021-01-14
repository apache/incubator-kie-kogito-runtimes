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

package org.kie.kogito.codegen.process.persistence.proto;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.data.Answer;
import org.kie.kogito.codegen.data.AnswerWitAnnotations;
import org.kie.kogito.codegen.data.Person;
import org.kie.kogito.codegen.data.PersonWithAddress;
import org.kie.kogito.codegen.data.PersonWithAddresses;
import org.kie.kogito.codegen.data.PersonWithList;
import org.kie.kogito.codegen.data.Question;
import org.kie.kogito.codegen.data.QuestionWithAnnotatedEnum;
import org.kie.kogito.codegen.process.persistence.MarshallerGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class MarshallerGeneratorTest {

    @Test
    void testPersonMarshallers() throws Exception {
        ProtoGenerator<Class<?>> generator = new ReflectionProtoGenerator(null, Collections.singleton(Person.class));

        Proto proto = generator.generate("org.kie.kogito.test");
        assertThat(proto).isNotNull();        
        assertThat(proto.getMessages()).hasSize(1);
        
        MarshallerGenerator marshallerGenerator = new MarshallerGenerator(this.getClass().getClassLoader());
        
        List<CompilationUnit> classes = marshallerGenerator.generate(proto.toString());
        assertThat(classes).isNotNull();       
        assertThat(classes).hasSize(1);
        
        Optional<ClassOrInterfaceDeclaration> marshallerClass = classes.get(0).getClassByName("PersonMessageMarshaller");
        assertThat(marshallerClass).isPresent();
    }

    @Test
    void testPersonWithListMarshallers() throws Exception {
        ProtoGenerator<Class<?>> generator = new ReflectionProtoGenerator(null, Collections.singleton(PersonWithList.class));

        Proto proto = generator.generate("org.kie.kogito.test");
        assertThat(proto).isNotNull();
        assertThat(proto.getMessages()).hasSize(1);

        System.out.println(proto.getMessages());

        MarshallerGenerator marshallerGenerator = new MarshallerGenerator(this.getClass().getClassLoader());

        List<CompilationUnit> classes = marshallerGenerator.generate(proto.toString());
        assertThat(classes).isNotNull();
        assertThat(classes).hasSize(1);

        Optional<ClassOrInterfaceDeclaration> marshallerClass = classes.get(0).getClassByName("PersonWithListMessageMarshaller");
        assertThat(marshallerClass).isPresent();
    }

    @Test
    void testPersonWithAddressMarshallers() throws Exception {
        ProtoGenerator<Class<?>> generator = new ReflectionProtoGenerator(null, Collections.singleton(PersonWithAddress.class));

        Proto proto = generator.generate("org.kie.kogito.test");
        assertThat(proto).isNotNull();        
        assertThat(proto.getMessages()).hasSize(2);
        
        MarshallerGenerator marshallerGenerator = new MarshallerGenerator(this.getClass().getClassLoader());
        
        List<CompilationUnit> classes = marshallerGenerator.generate(proto.toString());
        assertThat(classes).isNotNull();       
        assertThat(classes).hasSize(2);
        
        Optional<ClassOrInterfaceDeclaration> marshallerClass = classes.get(0).getClassByName("AddressMessageMarshaller");
        assertThat(marshallerClass).isPresent();
        marshallerClass = classes.get(1).getClassByName("PersonWithAddressMessageMarshaller");
        assertThat(marshallerClass).isPresent();
    }
    
    @Test
    void testPersonWithAddressesMarshallers() throws Exception {
        ProtoGenerator<Class<?>> generator = new ReflectionProtoGenerator(null, Collections.singleton(PersonWithAddresses.class));

        Proto proto = generator.generate("org.kie.kogito.test");
        assertThat(proto).isNotNull();        
        assertThat(proto.getMessages()).hasSize(2);

        System.out.println(proto.getMessages());
        
        MarshallerGenerator marshallerGenerator = new MarshallerGenerator(this.getClass().getClassLoader());
        
        List<CompilationUnit> classes = marshallerGenerator.generate(proto.toString());
        assertThat(classes).isNotNull();       
        assertThat(classes).hasSize(2);
        
        Optional<ClassOrInterfaceDeclaration> marshallerClass = classes.get(0).getClassByName("AddressMessageMarshaller");
        assertThat(marshallerClass).isPresent();
        marshallerClass = classes.get(1).getClassByName("PersonWithAddressesMessageMarshaller");
        assertThat(marshallerClass).isPresent();
    }

    @Test
    void testEnumInPojosMarshallers() throws Exception {
        Stream.of(Question.class, QuestionWithAnnotatedEnum.class).forEach(c -> {
            ProtoGenerator<Class<?>> generator = new ReflectionProtoGenerator(null, Collections.singleton(c));

            Proto proto = generator.generate("org.kie.kogito.test");
            assertThat(proto).isNotNull();
            assertThat(proto.getMessages()).hasSize(1);

            MarshallerGenerator marshallerGenerator = new MarshallerGenerator(this.getClass().getClassLoader());

            List<CompilationUnit> classes = null;
            try {
                classes = marshallerGenerator.generate(proto.toString());
            } catch (IOException e) {
                fail("Error generating marshaller for " + c.getName(), e);
            }
            assertThat(classes).isNotNull();
            assertThat(classes).hasSize(2);

            Optional<ClassOrInterfaceDeclaration> marshallerClass = classes.get(0).getClassByName(c.getSimpleName() + "MessageMarshaller");
            assertThat(marshallerClass).isPresent();
            String answerType = null;
            try {
                answerType = c.getDeclaredField("answer").getType().getSimpleName();
            } catch (NoSuchFieldException e) {
                fail("Unable to get answer field type for " + c.getName(), e);
            }
            marshallerClass = classes.get(1).getClassByName(answerType + "EnumMarshaller");
            assertThat(marshallerClass).isPresent();
        });
    }

    @Test
    void testEnumMarshallers() {
        Stream.of(Answer.class, AnswerWitAnnotations.class).forEach(e -> {
            ProtoGenerator<Class<?>> generator = new ReflectionProtoGenerator(null, Collections.singleton(e));

            Proto proto = generator.generate("org.kie.kogito.test");
            assertThat(proto).isNotNull();
            assertThat(proto.getEnums()).hasSize(1);

            MarshallerGenerator marshallerGenerator = new MarshallerGenerator(this.getClass().getClassLoader());

            List<CompilationUnit> classes = null;
            try {
                classes = marshallerGenerator.generate(proto.toString());
            } catch (IOException ex) {
                fail("Error generating marshaller for " + e.getName(), e);
            }
            assertThat(classes).isNotNull();
            assertThat(classes).hasSize(1);

            Optional<ClassOrInterfaceDeclaration> marshallerClass = classes.get(0).getClassByName(e.getSimpleName() + "EnumMarshaller");
            assertThat(marshallerClass).isPresent();
        });
    }
}
