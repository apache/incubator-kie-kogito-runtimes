package org.kie.kogito.tracing.decision.event;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNMessageType;
import org.kie.dmn.api.core.DMNResult;

public abstract class EvaluateEvent implements Serializable {

    private final String evaluationId;
    private final String modelName;
    private final String modelNamespace;
    private final Map<String, Object> context;
    private final Map<String, Object> contextMetadata;
    private final Result result;

    public EvaluateEvent(String evaluationId, String modelName, String modelNamespace, DMNContext context) {
        DMNContext clone = context.clone();
        this.evaluationId = evaluationId;
        this.modelName = modelName;
        this.modelNamespace = modelNamespace;
        this.context = clone.getAll();
        this.contextMetadata = clone.getMetadata().asMap();
        this.result = null;
    }

    public EvaluateEvent(String evaluationId, String modelName, String modelNamespace, DMNResult result) {
        DMNContext clone = result.getContext().clone();
        this.evaluationId = evaluationId;
        this.modelName = modelName;
        this.modelNamespace = modelNamespace;
        this.context = clone.getAll();
        this.contextMetadata = clone.getMetadata().asMap();
        this.result = from(result);
    }

    public String getEvaluationId() {
        return evaluationId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelNamespace() {
        return modelNamespace;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public Map<String, Object> getContextMetadata() {
        return contextMetadata;
    }

    public Result getResult() {
        return result;
    }

    public static class Result {

        private final List<DecisionResult> decisionResults;
        private final List<Message> messages;

        public Result(List<DecisionResult> decisionResults, List<Message> messages) {
            this.decisionResults = decisionResults;
            this.messages = messages;
        }

        public List<DecisionResult> getDecisionResults() {
            return decisionResults;
        }

        public List<Message> getMessages() {
            return messages;
        }
    }

    public static class DecisionResult {

        private final String decisionId;
        private final String decisionName;
        private final DMNDecisionResult.DecisionEvaluationStatus evaluationStatus;
        private final Object result;
        private final List<Message> messages;
        private final boolean errors;

        public DecisionResult(String decisionId, String decisionName, DMNDecisionResult.DecisionEvaluationStatus evaluationStatus, Object result, List<Message> messages, boolean errors) {
            this.decisionId = decisionId;
            this.decisionName = decisionName;
            this.evaluationStatus = evaluationStatus;
            this.result = result;
            this.messages = messages;
            this.errors = errors;
        }

        public String getDecisionId() {
            return decisionId;
        }

        public String getDecisionName() {
            return decisionName;
        }

        public DMNDecisionResult.DecisionEvaluationStatus getEvaluationStatus() {
            return evaluationStatus;
        }

        public Object getResult() {
            return result;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public boolean hasErrors() {
            return errors;
        }
    }

    public static class Message {

        private final DMNMessageType type;
        private final org.kie.api.builder.Message.Level level;
        private final String text;
        private final String sourceId;

        public Message(DMNMessageType type, org.kie.api.builder.Message.Level level, String text, String sourceId) {
            this.type = type;
            this.level = level;
            this.text = text;
            this.sourceId = sourceId;
        }

        public DMNMessageType getType() {
            return type;
        }

        public org.kie.api.builder.Message.Level getLevel() {
            return level;
        }

        public String getText() {
            return text;
        }

        public String getSourceId() {
            return sourceId;
        }
    }

    private static Result from(DMNResult result) {
        return new Result(
                map(result.getDecisionResults(), EvaluateEvent::from),
                map(result.getMessages(), EvaluateEvent::from)
        );
    }

    private static DecisionResult from(DMNDecisionResult dr) {
        return new DecisionResult(
                dr.getDecisionId(),
                dr.getDecisionName(),
                dr.getEvaluationStatus(),
                dr.getResult(),
                map(dr.getMessages(), EvaluateEvent::from),
                dr.hasErrors()
        );
    }

    private static Message from(DMNMessage msg) {
        return new Message(
                msg.getMessageType(),
                msg.getLevel(),
                msg.getText(),
                msg.getSourceId()
        );
    }

    private static <I, O> List<O> map(List<I> input, Function<I, O> mapper) {
        return input == null
                ? null
                : input.stream().map(mapper).collect(Collectors.toList());
    }

}
