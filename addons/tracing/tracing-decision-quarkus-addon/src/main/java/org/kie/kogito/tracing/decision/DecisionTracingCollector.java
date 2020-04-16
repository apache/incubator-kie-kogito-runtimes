package org.kie.kogito.tracing.decision;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.quarkus.vertx.ConsumeEvent;
import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.PublishSubject;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.EvaluateDecisionEvent;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DecisionTracingCollector {

    private static final Logger LOG = LoggerFactory.getLogger(DecisionTracingCollector.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

    private final Map<String, List<EvaluateEvent>> cacheMap = new HashMap<>();
    private final PublishSubject<String> eventSubject = PublishSubject.create();

    @Outgoing("kogito-tracing-decision")
    public Publisher<String> getEventPublisher() {
        return eventSubject.toFlowable(BackpressureStrategy.BUFFER);
    }

    @ConsumeEvent("kogito-tracing-decision_BeforeEvaluateAllEvent")
    public String handleEvent(BeforeEvaluateAllEvent event) {
        return handleEvaluateEvent(event);
    }

    @ConsumeEvent("kogito-tracing-decision_AfterEvaluateAllEvent")
    public String handleEvent(AfterEvaluateAllEvent event) {
        return handleEvaluateEvent(event);
    }

    @ConsumeEvent("kogito-tracing-decision_BeforeEvaluateDecisionEvent")
    public String handleEvent(org.kie.kogito.tracing.decision.event.BeforeEvaluateDecisionEvent event) {
        return handleEvaluateEvent(event);
    }

    @ConsumeEvent("kogito-tracing-decision_AfterEvaluateDecisionEvent")
    public String handleEvent(org.kie.kogito.tracing.decision.event.AfterEvaluateDecisionEvent event) {
        return handleEvaluateEvent(event);
    }

    public String handleEvaluateEvent(EvaluateEvent event) {
        try {
            if (LOG.isInfoEnabled()) {
                if (event instanceof EvaluateDecisionEvent) {
                    LOG.trace(
                            "Received {}(evaluationId: {}, modelName: {}, modelNamespace: {}, decisionId: {})",
                            event.getClass().getSimpleName(),
                            event.getExecutionId(),
                            event.getModelName(),
                            event.getModelNamespace(),
                            ((EvaluateDecisionEvent) event).getDecisionId()
                    );
                } else {
                    LOG.trace(
                            "Received {}(evaluationId: {}, modelName: {}, modelNamespace: {})",
                            event.getClass().getSimpleName(),
                            event.getExecutionId(),
                            event.getModelName(),
                            event.getModelNamespace()
                    );
                }
            }

            String evaluationId = event.getExecutionId();
            if (cacheMap.containsKey(evaluationId)) {
                cacheMap.get(evaluationId).add(event);
            } else {
                List<EvaluateEvent> list = new LinkedList<>();
                list.add(event);
                cacheMap.put(evaluationId, list);
                LOG.trace("Added evaluation {} to cache (current size: {})", evaluationId, cacheMap.size());
            }

            if (event instanceof AfterEvaluateAllEvent) {
                Pair<String, String> payload = aggregate(evaluationId, cacheMap.get(evaluationId));
                eventSubject.onNext(payload.getRight());
                LOG.debug("Generated aggregated event for evaluation {} (length {})", payload.getKey(), payload.getValue().length());
                cacheMap.remove(evaluationId);
                LOG.trace("Removed evaluation {} from cache (current size: {})", evaluationId, cacheMap.size());
            }

            return "";
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Pair<String, String> aggregate(String evaluationId, List<EvaluateEvent> events) {
        AfterEvaluateAllEvent event = (AfterEvaluateAllEvent) events.get(events.size() - 1);

        final CloudEvent<AfterEvaluateAllEvent> cloudEvent = new CloudEvent<>(
                evaluationId,
                AfterEvaluateAllEvent.class.getName(),
                sourceFrom(event),
                event
        );

        try {
            return Pair.of(evaluationId, MAPPER.writer().writeValueAsString(cloudEvent));
        } catch (JsonProcessingException e) {
            LOG.error("JsonProcessingException", e);
            e.printStackTrace();
            return Pair.of(evaluationId, "ERROR");
        }
    }

    private static URI sourceFrom(EvaluateEvent event) {
        return URI.create(String.format("%s/%s", event.getModelNamespace(), urlEncode(event.getModelName())));
    }

    private static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class CloudEvent<T> {
        private final String specversion;
        private final String id;
        private final String type;
        private final URI source;
        private final T data;

        public CloudEvent(String id, String type, URI source, T data) {
            this.specversion = "1.0";
            this.id = id;
            this.type = type;
            this.source = source;
            this.data = data;
        }

        public String getSpecversion() {
            return specversion;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public URI getSource() {
            return source;
        }

        public T getData() {
            return data;
        }

    }

}
