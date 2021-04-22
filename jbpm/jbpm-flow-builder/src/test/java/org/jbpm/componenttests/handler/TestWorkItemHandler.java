/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.componenttests.handler;

import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;

public class TestWorkItemHandler implements KogitoWorkItemHandler {
    private KogitoWorkItem workItem;
    private boolean aborted = false;

    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        this.workItem = workItem;
    }

    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        aborted = true;
    }

    public KogitoWorkItem getWorkItem() {
        return workItem;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void reset() {
        workItem = null;
    }
}
