package org.kie.kogito.yard;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractYaRDModels implements YaRDModels {

    protected Map<String, YaRDModel> content = new HashMap<>();

    @Override
    public YaRDModel getModel(String name) {
        return content.get(name);
    }

    protected void add(InputStreamReader ier) {
        final String yaml = new BufferedReader(ier).lines().collect(Collectors.joining("\n"));

        final YaRDModel model = new YaRDModel(yaml);
        content.put(model.getName(), model);
    }
}
