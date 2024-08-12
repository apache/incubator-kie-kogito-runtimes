package org.kie.kogito.usertask;

import org.kie.kogito.usertask.events.UserTaskAssignmentEvent;
import org.kie.kogito.usertask.events.UserTaskAttachmentEvent;
import org.kie.kogito.usertask.events.UserTaskCommentEvent;
import org.kie.kogito.usertask.events.UserTaskDeadlineEvent;
import org.kie.kogito.usertask.events.UserTaskStateEvent;
import org.kie.kogito.usertask.events.UserTaskVariableEvent;

public interface UserTaskEventListener {

    default void onUserTaskDeadline(UserTaskDeadlineEvent event) {
        // nothing
    }

    default void onUserTaskState(UserTaskStateEvent event) {
        // nothing
    }

    default void onUserTaskAssignment(UserTaskAssignmentEvent event) {
        // nothing
    }

    default void onUserTaskInputVariable(UserTaskVariableEvent event) {
        // nothing
    }

    default void onUserTaskOutputVariable(UserTaskVariableEvent event) {
        // nothing
    }

    default void onUserTaskAttachmentAdded(UserTaskAttachmentEvent event) {
        // nothing
    }

    default void onUserTaskAttachmentDeleted(UserTaskAttachmentEvent event) {
        // nothing
    }

    default void onUserTaskAttachmentChange(UserTaskAttachmentEvent event) {
        // nothing
    }

    default void onUserTaskCommentChange(UserTaskCommentEvent event) {
        // nothing
    }

    default void onUserTaskCommentAdded(UserTaskCommentEvent event) {
        // nothing
    }

    default void onUserTaskCommentDeleted(UserTaskCommentEvent event) {
        // nothing
    }
}
