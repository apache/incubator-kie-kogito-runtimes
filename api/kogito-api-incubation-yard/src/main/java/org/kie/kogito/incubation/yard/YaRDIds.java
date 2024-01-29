package org.kie.kogito.incubation.yard;

import org.kie.kogito.incubation.common.ComponentRoot;

public class YaRDIds implements ComponentRoot {

    public LocalYaRDId get(String name) {
        return new LocalYaRDId(name);
    }
}
