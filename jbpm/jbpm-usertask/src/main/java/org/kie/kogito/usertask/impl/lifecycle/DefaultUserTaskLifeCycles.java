package org.kie.kogito.usertask.impl.lifecycle;

import java.util.Collections;

import org.kie.kogito.usertask.lifecycle.UserTaskLifeCycle;
import org.kie.kogito.usertask.lifecycle.UserTaskLifeCycles;

public class DefaultUserTaskLifeCycles extends UserTaskLifeCycles {

    public DefaultUserTaskLifeCycles() {
        super("default", Collections::emptyIterator);
        registerUserTaskLifeCycle();

    }

    public DefaultUserTaskLifeCycles(String defaultUserTaskLifeCycleId, Iterable<UserTaskLifeCycle> userTaskLifeCycle) {
        super(defaultUserTaskLifeCycleId, userTaskLifeCycle);
        registerUserTaskLifeCycle();
    }

    private void registerUserTaskLifeCycle() {
        registerUserTaskLifeCycle("default", new DefaultUserTaskLifeCycle());
        registerUserTaskLifeCycle("ws-human-task", new WsHumanTaskLifeCycle());
    }
}
