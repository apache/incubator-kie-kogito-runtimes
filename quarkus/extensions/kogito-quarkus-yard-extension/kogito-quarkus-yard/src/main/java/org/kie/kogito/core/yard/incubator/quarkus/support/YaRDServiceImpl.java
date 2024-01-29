package org.kie.kogito.core.yard.incubator.quarkus.support;

import java.util.Map;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.common.MapDataContext;
import org.kie.kogito.incubation.yard.LocalYaRDId;
import org.kie.kogito.incubation.yard.services.YaRDService;
import org.kie.kogito.yard.YaRDModel;
import org.kie.kogito.yard.YaRDModels;

public class YaRDServiceImpl implements YaRDService {

    private final YaRDModels models;

    public YaRDServiceImpl(YaRDModels models) {
        this.models = models;
    }

    @Override
    public DataContext evaluate(LocalId localId, DataContext inputContext) {

        if (localId instanceof LocalYaRDId yaRDId) {
            final YaRDModel model = models.getModel(yaRDId.name());
            final Map<String, Object> payload = inputContext.as(MapDataContext.class).toMap();
            final Map<String, Object> evaluate = model.evaluate(payload);
            return MapDataContext.of(evaluate);
        } else {
            throw new IllegalArgumentException(
                    "Not a valid decision id " + localId.toLocalId().asLocalUri());
        }
    }
}
