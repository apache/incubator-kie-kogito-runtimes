package org.jbpm.bpmn2.test;

import java.util.Map;

import org.jbpm.bpmn2.JbpmBpmn2TestCase;
import org.jbpm.bpmn2.core.TextAnnotation;
import org.junit.jupiter.api.Test;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.ruleflow.core.Metadata.TEXT_ANNOTATIONS;

public class TextAnnotationTest extends JbpmBpmn2TestCase {

    @Test
    public void testTextAnnotationParsing() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/textAnnotation/BPMN2-TextAnnotation.bpmn2");
        KogitoProcessInstance pi = kruntime.startProcess("BPMN2_TextAnnotation"); // <-- use the process id from the BPMN

        @SuppressWarnings("unchecked")
        Map<String, TextAnnotation> annotations =
                (Map<String, TextAnnotation>) pi.getProcess().getMetaData().get(TEXT_ANNOTATIONS);

        assertThat(annotations).as("annotations map").isNotNull().isNotEmpty();

        assertThat(annotations).containsKey("note");
        assertThat(annotations.get("note").getText()).isEqualTo("textNote");
    }

    @Test
    public void testTextAnnotationParsingForBusinessRule() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/textAnnotation/BPMN2-TaskTextAnnotation.bpmn2");
        KogitoProcessInstance pi = kruntime.startProcess("BPMN2_TaskTextAnnotation"); // <-- use the process id from the BPMN

        @SuppressWarnings("unchecked")
        Map<String, TextAnnotation> annotations =
                (Map<String, TextAnnotation>) pi.getProcess().getMetaData().get(TEXT_ANNOTATIONS);

        assertThat(annotations).as("annotations map").isNotNull().isNotEmpty();

        assertThat(annotations).containsKey("taskTxtAnnotationId");
        assertThat(annotations.get("taskTxtAnnotationId").getText()).isEqualTo("TaskAnnotation");
    }

    @Test
    public void testTextAnnotationParsingForTimers() throws Exception {
        kruntime = createKogitoProcessRuntime("org/jbpm/bpmn2/textAnnotation/BPMN2-TimerTxtAnnotation.bpmn2");
        KogitoProcessInstance pi = kruntime.startProcess("BPMN2_TimerTxtAnnotation"); // <-- use the process id from the BPMN

        @SuppressWarnings("unchecked")
        Map<String, TextAnnotation> annotations =
                (Map<String, TextAnnotation>) pi.getProcess().getMetaData().get(TEXT_ANNOTATIONS);

        assertThat(annotations).as("annotations map").isNotNull().isNotEmpty();

        assertThat(annotations).containsKey("timerTextAnnotation");
        assertThat(annotations.get("timerTextAnnotation").getText()).isEqualTo("TimerText");
    }
}
