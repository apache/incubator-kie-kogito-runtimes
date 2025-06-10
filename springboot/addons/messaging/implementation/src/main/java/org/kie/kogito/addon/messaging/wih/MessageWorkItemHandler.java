package org.kie.kogito.addon.messaging.wih;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.kie.kogito.Application;
import org.kie.kogito.event.EventDispatcher;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.kie.kogito.internal.process.workitem.Policy;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;
import org.springframework.web.context.annotation.ApplicationScope;

@ApplicationScope
public class MessageWorkItemHandler extends DefaultKogitoWorkItemHandler {

    @AuroWired
    EventDispatcher dispatcher;
    
    @Override
    public Application getApplication() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setApplication(Application app) {
        // TODO Auto-generated method stub

    }

    @Override
    public Optional<WorkItemTransition> transitionToPhase(KogitoWorkItemManager manager, KogitoWorkItem workItem, WorkItemTransition transition) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public Set<String> allowedTransitions(String phaseStatus) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkItemTransition newTransition(String phaseId, String phaseStatus, Map<String, Object> map, Policy... policy) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkItemTransition startingTransition(Map<String, Object> data, Policy... policies) {
        dispatcher.dispatch(trigger, event);
        return null;
    }

    @Override
    public WorkItemTransition completeTransition(String phaseStatus, Map<String, Object> data, Policy... policies) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkItemTransition abortTransition(String phaseStatus, Policy... policies) {
        // TODO Auto-generated method stub
        return null;
    }

}
