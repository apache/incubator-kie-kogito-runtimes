package org.kie.kogito.serverless.workflow.monitoring;

import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.monitoring.core.common.process.MetricsProcessEventListener;
import org.kie.kogito.serverless.workflow.SWFConstants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

public class SonataFlowMetricProcessEventListener extends MetricsProcessEventListener {

    public SonataFlowMetricProcessEventListener(KogitoGAV gav, MeterRegistry meterRegistry) {
        super("sonataflow-process-monitoring-listener", gav, meterRegistry);
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        final KogitoProcessInstance processInstance = (KogitoProcessInstance) event.getProcessInstance();
        Object node = processInstance.getVariables().get(SWFConstants.DEFAULT_WORKFLOW_VAR);
        if (node instanceof ObjectNode) {
            registerObject(processInstance.getProcessId(), (ObjectNode) node, null);
        }

    }

    private void registerObject(String processId, ObjectNode node, String prefix) {
        node.fields().forEachRemaining(e -> registerInputParam(processId, e.getKey(), e.getValue(), prefix));
    }

    private void registerInputParam(String processId, String key, JsonNode value, String prefix) {
        if (value.isObject()) {
            registerObject(processId, (ObjectNode) value, concat(prefix, key));
        } else {
            registerInputParam(processId, concat(prefix, key), value.toString());
        }
    }

    private String concat(String prefix, String key) {
        return prefix == null ? key : prefix + "." + key;
    }

    private void registerInputParam(String processId, String key, String value) {
        buildCounter("sonataflow_input_parameters_counter", "Input parameters", processId, Tag.of("param_name", key), Tag.of("param_value", value)).increment();
    }
}
