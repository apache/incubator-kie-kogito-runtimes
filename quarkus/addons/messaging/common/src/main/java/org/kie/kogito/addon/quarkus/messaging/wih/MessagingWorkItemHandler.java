package org.kie.kogito.addon.quarkus.messaging.wih;

import java.util.Collections;
import java.util.Optional;

import org.kie.kogito.event.DataEventFactory;
import org.kie.kogito.event.EventEmitter;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;
import org.kie.kogito.process.workitems.impl.DefaultKogitoWorkItemHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class MessagingWorkItemHandler extends DefaultKogitoWorkItemHandler {

    public static String WORK_ITEM_HANDLER_EXTERNAL_NAME = "External Send Task";

    @Inject
    Instance<EventEmitter> emitters;

    @Override
    public Optional<WorkItemTransition> activateWorkItemHandler(KogitoWorkItemManager manager, KogitoWorkItemHandler handler, KogitoWorkItem workItem, WorkItemTransition transition) {
        String eventType = (String) workItem.getParameter("Signal");
        emitters.stream().filter(e -> e.channelName().equals(eventType)).forEach(e -> e.emit(DataEventFactory.from(workItem.getParameter("Data"))));
        return Optional.of(this.completeTransition(workItem.getPhaseStatus(), Collections.emptyMap()));
    }

    @Override
    public String getName() {
        return WORK_ITEM_HANDLER_EXTERNAL_NAME;
    }
}
