/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.codegen.process;

import java.util.stream.Stream;

import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.validation.ProcessValidator;
import org.jbpm.process.core.validation.ProcessValidatorRegistry;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.kogito.process.validation.ValidationException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class JavaRuleFlowProcessValidatorTest {

    @BeforeAll
    static void init() {
        ProcessValidatorRegistry.getInstance().registerAdditonalValidator(JavaRuleFlowProcessValidator.getInstance());
    }

    public static Stream<Arguments> invalidVariables() {
        return Stream.of(
                Arguments.of(new String[] {
                        "com.myspace.demo.Order order2 = null; System.out.println(\"Order has been created \" + order);java.util.Arrays.toString(new int[]{1, 2});System.out.println(orders);",
                        "uses unknown variable in the script: orders" }),
                Arguments.of(new String[] {
                        "a = 2",
                        "Parse error. Found \"}\", expected one of  \"!=\" \"%\" \"%=\" \"&\" \"&&\" \"&=\" \"*\" \"*=\" \"+\" \"+=\" \"-\" \"-=\" \"->\" \"/\" \"/=\" \"::\" \";\" \"<\" \"<<=\" \"<=\" \"=\" \"==\" \">\" \">=\" \">>=\" \">>>=\" \"?\" \"^\" \"^=\" \"instanceof\" \"|\" \"|=\" \"||\"" }),
                Arguments.of(new String[] {
                        "a = 2;",
                        "uses unknown variable in the script: a" }),
                Arguments.of(new String[] {
                        "a.toString(); Integer i = Integer.valueOf(\"1\");",
                        "uses unknown variable in the script: a" }),
                Arguments.of(new String[] {
                        "System.out.println(\"Order has been created \" + x);",
                        "uses unknown variable in the script: x" }),
                Arguments.of(new String[] {
                        "System.out.println(\"[\" + (new java.util.Date()) + \"] [\" + java.lang.Thread.currentThread().getName() +\"]\");\n" +
                                "java.util.ArrayList list = new java.util.ArrayList();\n" +
                                "System.out.println(Integer.valueOf(x));",
                        "uses unknown variable in the script: x" }));
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("invalidVariables")
    public void testScriptInvalidVariable(String script, String message) {
        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("demo.orders");
        factory
                .variable("order", new ObjectDataType("com.myspace.demo.Order"))
                .variable("approver", new ObjectDataType("String"))
                .name("orders")
                .packageName("com.myspace.demo")
                .dynamic(false)
                .version("1.0")
                .startNode(1)
                .name("start")
                .done()
                .actionNode(2)
                .name("Dump order 1")
                .action("java", script)
                .done()
                .endNode(3)
                .name("end")
                .terminate(false)
                .done()
                .connection(1, 2)
                .connection(2, 3);
        RuleFlowProcess process = factory.getProcess();
        ProcessValidator validator = ProcessValidatorRegistry.getInstance().getValidator(process, null);
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> validator.validate(process))
                .withMessageContaining(message);
    }
}
