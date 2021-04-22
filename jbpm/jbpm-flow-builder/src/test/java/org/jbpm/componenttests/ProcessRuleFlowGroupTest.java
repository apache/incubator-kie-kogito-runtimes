/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.componenttests;

import java.io.Reader;
import java.io.StringReader;

import org.drools.core.io.impl.ReaderResource;
import org.jbpm.componenttests.test.Person;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Test;
import org.kie.api.io.ResourceType;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessRuleFlowGroupTest extends AbstractBaseTest {

    @Test
    public void testRuleSetProcessContext() throws Exception {
        Reader source = new StringReader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                        "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                        "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.ruleset\" package-name=\"org.jbpm\" version=\"1\" >\n" +
                        "\n" +
                        "  <header>\n" +
                        "  </header>\n" +
                        "\n" +
                        "  <nodes>\n" +
                        "    <start id=\"1\" name=\"Start\" />\n" +
                        "    <ruleSet id=\"2\" name=\"RuleSet\" ruleFlowGroup=\"MyGroup\" >\n" +
                        "    </ruleSet>\n" +
                        "    <end id=\"3\" name=\"End\" />\n" +
                        "  </nodes>\n" +
                        "\n" +
                        "  <connections>\n" +
                        "    <connection from=\"1\" to=\"2\" />\n" +
                        "    <connection from=\"2\" to=\"3\" />\n" +
                        "  </connections>\n" +
                        "\n" +
                        "</process>");
        Reader source2 = new StringReader(
                "package org.jbpm;\n" +
                        "\n" +
                        "import org.jbpm.componenttests.test.Person;\n" +
                        "import org.kie.api.runtime.process.ProcessContext;\n" +
                        "\n" +
                        "rule MyRule ruleflow-group \"MyGroup\" dialect \"mvel\" \n" +
                        "  when\n" +
                        "    Person( age > 25 )\n" +
                        "  then\n" +
                        "    System.out.println(drools.getContext(ProcessContext).getProcessInstance().getProcessName());\n" +
                        "end");
        builder.add(new ReaderResource(source), ResourceType.DRF);
        builder.add(new ReaderResource(source2), ResourceType.DRL);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime();
        kruntime.getKieRuntime().getEnvironment().set("org.jbpm.rule.task.waitstate", "true");
        Person person = new Person();
        person.setAge(30);
        kruntime.getKieSession().insert(person);
        // start process
        KogitoProcessInstance processInstance = kruntime.startProcess("org.drools.ruleset");
        assertEquals(KogitoProcessInstance.STATE_ACTIVE, processInstance.getState());
        kruntime.getKieSession().fireAllRules();
        assertEquals(KogitoProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

}
