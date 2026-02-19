// Copyright IBM Corp. 2025.

package org.acme;

import org.kie.kogito.usertask.UserTaskEventListener;
import org.kie.kogito.usertask.events.UserTaskStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// demonstrates transactional behavior of UserTaskEventListener, it will throw a RuntimeException when a task transitions to "Completed" status and verifies that the transaction rolls back, leaving the task in its previous state.

@Component
public class FailingUserTaskEventListener implements UserTaskEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailingUserTaskEventListener.class);
    private static final String PREFIX = ">>>>> [FAILING-LISTENER] ";

    @Value("${app.listener.fail-on-complete:false}")
    boolean failOnComplete;

    @Override
    public void onUserTaskState(UserTaskStateEvent event) {
        String newStatus = event.getNewStatus() != null ? event.getNewStatus().getName() : null;

        LOGGER.info(PREFIX + "onUserTaskState: taskName={}, newStatus={}, failOnComplete={}",
                event.getUserTaskInstance().getTaskName(),
                newStatus,
                failOnComplete);

        if (failOnComplete && "Completed".equals(newStatus)) {
            throw new RuntimeException("Simulated external system failure. " +
                    "Transaction should rollback and task should remain in previous state.");
        }
    }
}
