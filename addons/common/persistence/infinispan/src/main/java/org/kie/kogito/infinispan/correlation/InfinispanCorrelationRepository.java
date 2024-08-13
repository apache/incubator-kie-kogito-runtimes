package org.kie.kogito.infinispan.correlation;

import java.io.UncheckedIOException;
import java.util.Optional;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.kie.kogito.correlation.CompositeCorrelation;
import org.kie.kogito.correlation.Correlation;
import org.kie.kogito.correlation.CorrelationInstance;
import org.kie.kogito.correlation.SimpleCorrelation;
import org.kie.kogito.internal.utils.ConversionUtils;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class InfinispanCorrelationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfinispanCorrelationRepository.class);

    private static final String ENCODED_CORRELATION_ID_FIELD = "encodedCorrelationId";
    private static final String CORRELATED_ID_FIELD = "correlatedId";
    private static final String CORRELATION_FIELD = "correlation";
    public static final String CORRELATIONS_CACHE_NAME = "correlations";

    private final RemoteCache<String, String> cache;
    private final ObjectMapper objectMapper;

    public InfinispanCorrelationRepository(
            RemoteCacheManager cacheManager,
            String templateName) {
        if (ConversionUtils.isEmpty(templateName)) {
            this.cache = cacheManager.administration().getOrCreateCache(CORRELATIONS_CACHE_NAME, DefaultTemplate.LOCAL);
        } else {
            this.cache = cacheManager.administration().getOrCreateCache(CORRELATIONS_CACHE_NAME, templateName);
        }
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addAbstractTypeMapping(Correlation.class, SimpleCorrelation.class);
        this.objectMapper = ObjectMapperFactory.get().copy().registerModule(simpleModule);
    }

    public CorrelationInstance insert(String encodedCorrelationId, String correlatedId, Correlation correlation) {
        CorrelationInstance correlationInstance = new CorrelationInstance(encodedCorrelationId, correlatedId, correlation);

        try {
            String json = this.objectMapper.writeValueAsString(correlation);
            CorrelationRecord correlationRecord = new CorrelationRecord(
                    encodedCorrelationId, correlatedId, json);
            String value = this.objectMapper.writeValueAsString(correlationRecord);
            this.cache.put(encodedCorrelationId, value);
            return correlationInstance;
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public CorrelationInstance findByEncodedCorrelationId(String encodedCorrelationId) {
        String json = this.cache.get(encodedCorrelationId);
        if (json == null) {
            return null;
        }
        try {
            CorrelationRecord correlation = this.objectMapper.readValue(json, CorrelationRecord.class);
            CompositeCorrelation compositeCorrelation = this.objectMapper.readValue(correlation.correlation(), CompositeCorrelation.class);
            return new CorrelationInstance(
                    correlation.encodedCorrelationId(),
                    correlation.correlatedId(),
                    compositeCorrelation);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public CorrelationInstance findByCorrelatedId(String correlatedId) {

        Optional<String> first = this.cache.values().stream().filter(item -> item.contains("\"correlatedId\":\"%s\"".formatted(correlatedId)))
                .findFirst();
        if (first.isEmpty()) {
            return null;
        }

        try {
            CorrelationRecord correlationRecord = this.objectMapper.readValue(first.get(), CorrelationRecord.class);
            CompositeCorrelation compositeCorrelation = this.objectMapper.readValue(correlationRecord.correlation(), CompositeCorrelation.class);
            return new CorrelationInstance(
                    correlationRecord.encodedCorrelationId(),
                    correlationRecord.correlatedId(),
                    compositeCorrelation);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void delete(String encodedCorrelationId) {
        this.cache.remove(encodedCorrelationId);
    }
}
