package org.kie.kogito.infinispan.correlation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.kie.kogito.correlation.Correlation;
import org.kie.kogito.correlation.CorrelationEncoder;
import org.kie.kogito.correlation.CorrelationInstance;
import org.kie.kogito.correlation.CorrelationService;
import org.kie.kogito.event.correlation.MD5CorrelationEncoder;
import org.kie.kogito.internal.utils.ConversionUtils;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InfinispanCorrelationService implements CorrelationService {

    private static final String CORRELATIONS_CACHE_NAME = "correlations";
    private final RemoteCache<String, String> cache;
    private final CorrelationEncoder encoder;
    private final ObjectMapper objectMapper;

    public InfinispanCorrelationService(RemoteCacheManager remoteCacheManager, String templateName) {
        if (ConversionUtils.isEmpty(templateName)) {
            this.cache = remoteCacheManager.administration().getOrCreateCache(CORRELATIONS_CACHE_NAME, DefaultTemplate.LOCAL);
        } else {
            this.cache = remoteCacheManager.administration().getOrCreateCache(CORRELATIONS_CACHE_NAME, templateName);
        }
        this.encoder = new MD5CorrelationEncoder();
        this.objectMapper = ObjectMapperFactory.get().copy();
    }

    @Override
    public CorrelationInstance create(Correlation correlation, String correlatedId) {
        String encodedCorrelationId = this.encoder.encode(correlation);

        String id = UUID.randomUUID().toString();
        Map<String, Object> object = Map.of(
                "id", id,
                "encodedCorrelationId", encodedCorrelationId,
                "correlatedId", correlatedId,
                "correlation", correlation);

        try {
            String json = this.objectMapper.writeValueAsString(object);
            this.cache.put(id, json);
            return new CorrelationInstance(encodedCorrelationId, correlatedId, correlation);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<CorrelationInstance> find(Correlation correlation) {
        return Optional.empty();
    }

    @Override
    public Optional<CorrelationInstance> findByCorrelatedId(String correlatedId) {
        return Optional.empty();
    }

    @Override
    public void delete(Correlation correlation) {

    }
}
