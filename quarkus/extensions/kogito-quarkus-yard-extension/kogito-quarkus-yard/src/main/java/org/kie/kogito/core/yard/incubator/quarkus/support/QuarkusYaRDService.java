package org.kie.kogito.core.yard.incubator.quarkus.support;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.yard.services.YaRDService;
import org.kie.kogito.yard.YaRDModels;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class QuarkusYaRDService implements YaRDService {

    @Inject
    Instance<YaRDModels> models;

    YaRDServiceImpl delegate;

    @PostConstruct
    void startup() {
        this.delegate = new YaRDServiceImpl(models.get());
    }

    @Override
    public DataContext evaluate(LocalId localId, DataContext inputContext) {
        return delegate.evaluate(localId, inputContext);
    }
}
