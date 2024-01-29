package org.kie.kogito.incubation.yard;

import org.kie.kogito.incubation.common.Id;
import org.kie.kogito.incubation.common.LocalUri;
import org.kie.kogito.incubation.common.LocalUriId;

public class LocalYaRDId extends LocalUriId implements Id {
    public static final String PREFIX = "YARD";
    private final String name;

    public LocalYaRDId(String name) {
        super(makeLocalUri(name));
        this.name = name;
    }

    public String name() {
        return name;
    }

    private static LocalUri makeLocalUri(String name) {
        return LocalUri.Root.append(PREFIX).append(name);
    }
}
