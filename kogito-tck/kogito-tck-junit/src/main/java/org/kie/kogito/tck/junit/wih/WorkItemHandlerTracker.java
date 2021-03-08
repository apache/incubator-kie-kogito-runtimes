/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tck.junit.wih;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.process.workitem.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkItemHandlerTracker implements KogitoWorkItemHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkItemHandlerTracker.class);

    private final List<KogitoWorkItem> workItems;

    public WorkItemHandlerTracker() {
        workItems = Collections.synchronizedList(new ArrayList<>());
    }

    public String getFirstId() {
        return workItems.get(0).getStringId();
    }
    public Collection<KogitoWorkItem> getWorkItems() {
        synchronized (workItems) {
            return Collections.unmodifiableCollection(new ArrayList<>(workItems));
        }
    }

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        LOGGER.info("executing: " + workItem.getStringId());
        workItems.add(workItem);
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        LOGGER.info("aborting: " + workItem.getStringId());
        workItems.remove(workItem);
    }

    public String getIdForNodeName(String nodeName) {
        for(KogitoWorkItem kwi : workItems) {
            if(kwi.getState() == KogitoWorkItem.PENDING && kwi.getNodeInstance().getNodeName().equals(nodeName)) {
                return kwi.getStringId();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(KogitoWorkItem kwi : workItems) {
            builder.append(kwi);
            builder.append("\n");
        }
        return builder.toString();
    }

}
