package org.kie.kogito.serverless.workflow.dmn;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jbpm.workflow.core.impl.NodeIoHelper;
import org.jbpm.workflow.instance.node.RuleSetNodeInstance;
import org.jbpm.workflow.instance.rule.DecisionRuleTypeEngine;
import org.kie.api.runtime.KieSession;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.dmn.DmnDecisionModel;
import org.kie.kogito.dmn.rest.DMNJSONUtils;
import org.kie.kogito.jackson.utils.JsonObjectUtils;
import org.kie.kogito.serverless.workflow.SWFConstants;

public class SWFDecisionEngine implements DecisionRuleTypeEngine {

    @Override
    public void evaluate(RuleSetNodeInstance rsni, String inputNamespace, String inputModel, String decision) {
        String namespace = rsni.resolveExpression(inputNamespace);
        String model = rsni.resolveExpression(inputModel);

        DecisionModel modelInstance =
                Optional.ofNullable(rsni.getRuleSetNode().getDecisionModel())
                        .orElse(() -> new DmnDecisionModel(
                                ((KieSession) getKieRuntime(rsni)).getKieRuntime(DMNRuntime.class),
                                namespace,
                                model))
                        .get();

        //Input Binding
        DMNContext context = DMNJSONUtils.ctx(modelInstance, JsonObjectUtils.convertValue(getInputs(rsni).get(SWFConstants.MODEL_WORKFLOW_VAR), Map.class));
        DMNResult dmnResult = modelInstance.evaluateAll(context);
        if (dmnResult.hasErrors()) {
            String errors = dmnResult.getMessages(DMNMessage.Severity.ERROR).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            throw new RuntimeException("DMN result errors:: " + errors);
        }
        //Output Binding
        Map<String, Object> outputSet = Map.of(SWFConstants.RESULT, dmnResult.getContext().getAll());
        NodeIoHelper.processOutputs(rsni, outputSet::get, rsni::getVariable);

        rsni.triggerCompleted();
    }

}
