package org.jbpm.bpmn2;

import org.jbpm.bpmn2.textAnnotation.*;
import org.jbpm.test.utils.ProcessTestHelper;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;

import static org.assertj.core.api.Assertions.assertThat;

public class TextAnnotationTest extends JbpmBpmn2TestCase {
    @Test
    public void testTextAnnotationProcess() {
        Application app = ProcessTestHelper.newApplication();

        org.kie.kogito.process.Process<BPMN2_TextAnnotationModel> minimalProcess = BPMN2_TextAnnotationProcess.newProcess(app);
        BPMN2_TextAnnotationModel model = minimalProcess.createModel();
        org.kie.kogito.process.ProcessInstance<BPMN2_TextAnnotationModel> instance = minimalProcess.createInstance(model);
        instance.start();
        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_COMPLETED);
    }

    @Test
    public void testTextAnnotationProcessForTimers() {
        Application app = ProcessTestHelper.newApplication();

        org.kie.kogito.process.Process<BPMN2_TimerTxtAnnotationModel> minimalProcess = BPMN2_TimerTxtAnnotationProcess.newProcess(app);
        BPMN2_TimerTxtAnnotationModel model = minimalProcess.createModel();
        org.kie.kogito.process.ProcessInstance<BPMN2_TimerTxtAnnotationModel> instance = minimalProcess.createInstance(model);
        instance.start();
        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_ACTIVE);
    }

    @Test
    public void testTextAnnotationProcessForTasks() {
        Application app = ProcessTestHelper.newApplication();

        org.kie.kogito.process.Process<BPMN2_TaskTextAnnotationModel> minimalProcess = BPMN2_TaskTextAnnotationProcess.newProcess(app);
        BPMN2_TaskTextAnnotationModel model = minimalProcess.createModel();
        org.kie.kogito.process.ProcessInstance<BPMN2_TaskTextAnnotationModel> instance = minimalProcess.createInstance(model);
        instance.start();
        assertThat(instance.status()).isEqualTo(org.kie.kogito.process.ProcessInstance.STATE_ACTIVE);
    }

}
