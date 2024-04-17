package org.kie.kogito.serverless.workflow.dmn;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.parser.types.DMNTypeHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.serverlessworkflow.api.Workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.custom;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;

public class SWFDMNTest {
    @Test
    void testDMNFile() throws IOException {
        doIt(buildWorkflow(Collections.emptyMap()));
    }

    @Test
    void testDMNFileWithArgs() throws IOException {
        doIt(buildWorkflow(Map.of("Driver", ".Driver", "Violation", ".Violation")));
    }

    @Test
    void testDMNFileWithExprArg() throws IOException {
        doIt(buildWorkflow("{Driver:.Driver,Violation:.Violation}"));
    }

    private void doIt(Workflow workflow) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            JsonNode response = application.execute(workflow, Map.of("Driver", Map.of("Name", "Pepe", "Age", 19, "Points", 0, "State", "Spain", "City", "Zaragoza"), "Violation", Map.of("Code", "12",
                    "Date", new Date(System.currentTimeMillis()), "Type", "parking"))).getWorkflowdata();
            assertThat(response.get("Should the driver be suspended?")).isEqualTo(new TextNode("No"));
            response = application.execute(workflow, Map.of("Driver", Map.of("Name", "Pepe", "Age", 19, "Points", 19, "State", "Spain", "City", "Zaragoza"), "Violation", Map.of("Code", "12",
                    "Date", new Date(System.currentTimeMillis()), "Type", "speed", "Speed Limit", "120", "Actual Speed", "180"))).getWorkflowdata();
            assertThat(response.get("Should the driver be suspended?")).isEqualTo(new TextNode("Yes"));
        }
    }

    private Workflow buildWorkflow(Object args) {
        return workflow("PlayingWithDMN")
                .start(operation().action(call(custom("DMNTest", "dmn").metadata(DMNTypeHandler.FILE, "classpath:valid_models/DMNv1_x/Traffic Violation Simple.dmn")
                        .metadata(DMNTypeHandler.MODEL, "Traffic Violation")
                        .metadata(DMNTypeHandler.NAMESPACE, "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF"), args)))
                .end().build();
    }
}
