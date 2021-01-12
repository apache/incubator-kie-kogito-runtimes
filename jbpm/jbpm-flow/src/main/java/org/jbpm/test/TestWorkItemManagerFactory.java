package org.jbpm.test;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItemManagerFactory;
import org.kie.kogito.process.workitems.KogitoWorkItemManager;
import org.kie.kogito.process.workitems.impl.KogitoDefaultWorkItemManager;

public class TestWorkItemManagerFactory implements WorkItemManagerFactory {

    public KogitoWorkItemManager createWorkItemManager(InternalKnowledgeRuntime kruntime) {
        return new KogitoDefaultWorkItemManager(kruntime);
    }
}
