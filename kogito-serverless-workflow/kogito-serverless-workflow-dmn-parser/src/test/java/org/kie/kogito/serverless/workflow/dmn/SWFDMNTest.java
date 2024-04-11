package org.kie.kogito.serverless.workflow.dmn;

import java.io.IOException;
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
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            Workflow workflow = workflow("PlayingWithDMN")
                    .start(operation().action(call(custom("DMNTest", "dmn").metadata(DMNTypeHandler.FILE, "Traffic_Violation.dmn")
                            .metadata(DMNTypeHandler.MODEL, "Traffic Violation")
                            .metadata(DMNTypeHandler.NAMESPACE, "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF"))))
                    .end().build();
            JsonNode response = application.execute(workflow, Map.of("Driver", Map.of("Name", "Pepe", "Age", 19, "Points", 0, "State", "Spain", "City", "Zaragoza"), "Violation", Map.of("Code", "12",
                    "Date", new Date(System.currentTimeMillis()), "Type", "parking"))).getWorkflowdata();
            assertThat(response.get("Should the driver be suspended?")).isEqualTo(new TextNode("No"));
        }
    }
}
