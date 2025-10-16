package org.kie.kogito.usertask.lifecycle;

import java.util.HashMap;
import java.util.Map;

public class UserTaskLifeCycleRegistry {

    private static final Map<String, Class<? extends UserTaskLifeCycle>> REGISTRY = new HashMap<>();

    private UserTaskLifeCycleRegistry() {
    }

    public static void register(String name, Class<? extends UserTaskLifeCycle> clazz) {
        REGISTRY.put(name, clazz);
    }

    public static Class<? extends UserTaskLifeCycle> get(String name) {
        return REGISTRY.get(name);
    }
}
