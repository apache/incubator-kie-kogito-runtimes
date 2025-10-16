package org.kie.kogito.usertask.impl.lifecycle;

import java.util.Map;

import org.kie.kogito.usertask.lifecycle.UserTaskLifeCycle;

public class UserTaskLifeCycleRegistry {

    private static final Map<String, UserTaskLifeCycle> REGISTRY = Map.of(
            "default", new DefaultUserTaskLifeCycle(),
            "ws-human-task", new WsHumanTaskLifeCycle());

    private UserTaskLifeCycleRegistry() {
    }

    public static UserTaskLifeCycle get(String name) {
        return REGISTRY.get(name);
    }
}
