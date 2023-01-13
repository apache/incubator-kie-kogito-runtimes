/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.workflows.services;

import javax.enterprise.context.ApplicationScoped;

import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;

import static java.lang.String.format;

@ApplicationScoped
public class NodeInstanceTriggerEventListener extends DefaultKogitoProcessEventListener {

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        KogitoNodeInstance ni = (KogitoNodeInstance) event.getNodeInstance();
        if (ni.getTriggerTime() == null) {
            throw new IllegalStateException(format("Node instance for node %s, contains a null trigger time", ni.getNodeName()));
        }
    }
}
