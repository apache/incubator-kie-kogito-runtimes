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

package org.kie.kogito.process.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.codegen.AbstractCodegenTest;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.casemgmt.AdHocFragment;
import org.kie.kogito.process.casemgmt.Stage;
import org.kie.kogito.process.workitem.Policy;
import org.kie.kogito.services.identity.StaticIdentityProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.kie.kogito.process.casemgmt.ItemDescription.Status.AVAILABLE;
import static org.kie.kogito.process.casemgmt.ItemDescription.Status.COMPLETED;
import static org.kie.kogito.process.impl.ProcessTestUtils.assertState;

class StageTest extends AbstractCodegenTest {

    private Policy<?> securityPolicy = SecurityPolicy.of(new StaticIdentityProvider("role"));

    @Test
    void testSimpleAdHoc() throws Exception {
        Application app = generateCodeProcessesOnly("cases/SimpleAdHoc.bpmn");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.processes().processById("TestCase.SimpleAdHoc");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);

        HumanTaskNode task = new HumanTaskNode();
        task.setName("Update driver name");
        Stage expected = new Stage.Builder("_866F4F98-8810-42FE-8398-6E7E272523D9")
                .withName("Sub-process")
                .withStatus(AVAILABLE)
                .withAutoComplete(true)
                .withFragment(new HumanTaskNode(), "Update driver name")
                .build();
        Collection<Stage> stages = processInstance.stages();
        assertThat(stages.size()).isEqualTo(1);
        assertContainsStage(expected, stages);

        processInstance.start();
        assertState(processInstance, ProcessInstance.STATE_ACTIVE);
        assertThat(processInstance.workItems().size()).isEqualTo(1);
        WorkItem workItem = processInstance.workItems(securityPolicy).get(0);

        Map<String, Object> variables = new HashMap<>();
        variables.put("updatedName", "Paul");
        processInstance.completeWorkItem(workItem.getId(), variables, securityPolicy);

        assertThat(((Model) processInstance.variables()).toMap().get("driver")).isEqualTo("Paul");
        assertThat(processInstance.workItems().size()).isEqualTo(0);
        assertState(processInstance, ProcessInstance.STATE_COMPLETED);
        expected = new Stage.Builder(expected).withStatus(COMPLETED).build();
        assertContainsStage(expected, processInstance.stages());
    }

    @Test
    void testMultipleAdHoc() throws Exception {
        Application app = generateCodeProcessesOnly("cases/MultipleAdHoc.bpmn");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.processes().processById("TestCase.MultipleAdHoc");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("caseFile_currentStage", 0);
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        Collection<Stage> stages = processInstance.stages();
        assertThat(stages.size()).isEqualTo(3);
        ActionNode task = new ActionNode();
        task.setName("Task1");
        Collection<Stage> expected = buildExpectedMultipleAdHoc();
        expected.forEach(eStage -> assertContainsStage(eStage, processInstance.stages()));
        processInstance.start();

        assertThat(((Model) processInstance.variables()).toMap().get("caseFile_currentStage")).isEqualTo(3);
        assertThat(processInstance.workItems().size()).isEqualTo(0);
        assertState(processInstance, ProcessInstance.STATE_COMPLETED);
        expected = expected.stream().map(s -> new Stage.Builder(s).withStatus(COMPLETED).build()).collect(Collectors.toList());
        expected.forEach(eStage -> assertContainsStage(eStage, processInstance.stages()));
    }

    private Collection<Stage> buildExpectedMultipleAdHoc() {
        Collection<Stage> expected = new ArrayList<>();
        expected.add(new Stage.Builder("_4A154E74-F085-4ECA-93F1-DE452E624FB1").withName("Stage 1").withStatus(AVAILABLE).withAutoComplete(true).withFragment(new ActionNode(), "Task1").build());
        expected.add(new Stage.Builder("_168F4098-8ACA-4E81-9F97-5EAAC0782574").withName("Stage 2").withStatus(AVAILABLE).withAutoComplete(true).withFragment(new ActionNode(), "Task2").build());
        expected.add(new Stage.Builder("_E2F977AC-205A-4314-A406-00CEA619CDF9").withName("Stage 3").withStatus(AVAILABLE).withAutoComplete(true).withFragment(new ActionNode(), "Task3").build());
        return expected;
    }

    @Test
    void testCaseFile() throws Exception {
        Application app = generateCodeProcessesOnly("cases/CaseFileAdHoc.bpmn");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.processes().processById("TestCase.CaseFileAdHoc");

        Model m = p.createModel();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("caseFile_name", "Foo");
        parameters.put("name", "Bar");
        m.fromMap(parameters);

        ProcessInstance<?> processInstance = p.createInstance(m);
        Stage expected = new Stage.Builder("_E9418F90-1D23-4A0D-B8C5-A7939C34EB79")
                .withName("Sub-process")
                .withStatus(AVAILABLE)
                .withAutoComplete(true)
                .withFragment(new ActionNode(), "Sub process update")
                .build();

        Collection<Stage> stages = processInstance.stages();
        assertThat(stages.size()).isEqualTo(1);
        assertContainsStage(expected, stages);

        processInstance.start();

        //Process variable should not be updated in the sub-process
        assertThat(((Model) processInstance.variables()).toMap().get("name")).isEqualTo("Bar-process-subprocess");
        //CaseFile variable should be updated in the sub-process
        assertThat(((Model) processInstance.variables()).toMap().get("caseFile_name")).isEqualTo("Foo-process-subprocess");
        assertState(processInstance, ProcessInstance.STATE_COMPLETED);

        expected = new Stage.Builder(expected).withStatus(COMPLETED).build();
        stages = processInstance.stages();
        assertThat(stages.size()).isEqualTo(1);
        assertContainsStage(expected, stages);
    }

    @Test
    void testManualStages() throws Exception {
        Application app = generateCodeProcessesOnly("cases/ManualStages.bpmn");
        assertThat(app).isNotNull();

        Process<? extends Model> p = app.processes().processById("TestCase.ManualStages");

        ProcessInstance<?> processInstance = p.createInstance(p.createModel());
        Collection<Stage> expected = buildExpectedManualAdHoc();
        expected.forEach(eStage -> assertContainsStage(eStage, processInstance.stages()));

        processInstance.start();

        expected.forEach(eStage -> assertContainsStage(eStage, processInstance.stages()));
        WorkItem workItem = processInstance.workItems(securityPolicy).get(0);
        Map<String, Object> variables = new HashMap<>();
        variables.put("driver", "joe");
        processInstance.completeWorkItem(workItem.getId(), variables, securityPolicy);
        assertThat(((Model) processInstance.variables()).toMap().get("caseFile_name")).isEqualTo("joe");
        assertState(processInstance, ProcessInstance.STATE_ACTIVE);

        expected = expected.stream()
                .map(s -> {
                    if (s.getName().equals("Sub-process 1")) {
                        return new Stage.Builder(s).withStatus(COMPLETED).build();
                    }
                    return s;
                }).collect(Collectors.toList());
        expected.forEach(eStage -> assertContainsStage(eStage, processInstance.stages()));

        workItem = processInstance.workItems(securityPolicy).get(0);
        variables = new HashMap<>();
        variables.put("driver", "jane");
        processInstance.completeWorkItem(workItem.getId(), variables, securityPolicy);

        assertThat(((Model) processInstance.variables()).toMap().get("caseFile_name")).isEqualTo("jane");
        expected = expected.stream().map(s -> new Stage.Builder(s).withStatus(COMPLETED).build()).collect(Collectors.toList());
        expected.forEach(eStage -> assertContainsStage(eStage, processInstance.stages()));
    }

    private Collection<Stage> buildExpectedManualAdHoc() {
        Collection<Stage> expected = new ArrayList<>();
        expected.add(new Stage.Builder("_242BDE71-71A1-4354-BBBE-BF2F34A61E14")
                .withName("Sub-process 1")
                .withStatus(AVAILABLE)
                .withAutoComplete(false)
                .withCompletionExpression("caseFile_name.equals(\"joe\")")
                .withFragment(new HumanTaskNode(), "Task 1")
                .build());
        expected.add(new Stage.Builder("_852C704B-2FCA-4B87-8292-C9CA1634F19E")
                .withName("Sub-process 2")
                .withStatus(AVAILABLE)
                .withAutoComplete(true)
                .withFragment(new HumanTaskNode(), "Task 2")
                .build());
        return expected;
    }

    private static void assertContainsStage(Stage expected, Collection<Stage> stages) {
        if (expected == null) {
            assertThat(stages).isEmpty();
        }
        try {
            Optional<Stage> current = stages.stream().filter(s -> Objects.equals(expected.getId(), s.getId())).findFirst();
            assertTrue(current.isPresent(), expected.getId());
            assertThat(current.get()).isNotNull();
            assertThat(current.get().getId())
                    .withFailMessage("Id - Expected: %s - Got: %s", expected.getId(), current.get().getId())
                    .isEqualTo(expected.getId());
            assertThat(current.get().getName())
                    .withFailMessage("Name - Expected: %s - Got: %s", expected.getName(), current.get().getName())
                    .isEqualTo(expected.getName());
            assertThat(current.get().getStatus())
                    .withFailMessage("Status - Expected: %s - Got: %s", expected.getStatus(), current.get().getStatus())
                    .isEqualByComparingTo(expected.getStatus());
            assertThat(current.get().getActivationExpression())
                    .withFailMessage("ActivationExpression - Expected: %s - Got: %s", expected.getActivationExpression(), current.get().getActivationExpression())
                    .isEqualTo(expected.getActivationExpression());
            assertThat(current.get().getCompletionExpression())
                    .withFailMessage("CompletionExpression - Expected: %s - Got: %s", expected.getCompletionExpression(), current.get().getCompletionExpression())
                    .isEqualTo(expected.getCompletionExpression());
            assertThat(current.get().getAutoComplete())
                    .withFailMessage("Autocomplete - Expected: %s - Got: %s", expected.getAutoComplete(), current.get().getAutoComplete())
                    .isEqualTo(expected.getAutoComplete());
            assertAdHocFragments(expected.getAdHocFragments(), current.get().getAdHocFragments());
        } catch (AssertionError e) {
            fail(expected.getId(), e);
        }
    }

    private static void assertAdHocFragments(Collection<AdHocFragment> expected, Collection<AdHocFragment> current) {
        if (expected == null) {
            assertThat(current).isNull();
        }
        assertThat(current).isNotNull();
        assertThat(current.size()).isEqualTo(expected.size());
        expected.forEach(e -> assertTrue(
                current.stream().anyMatch(c -> c.getName().equals(e.getName()) && c.getType().equals(e.getType())),
                "Expected: " + e.toString() + ", Got: " + current.toString())
        );
    }
}
