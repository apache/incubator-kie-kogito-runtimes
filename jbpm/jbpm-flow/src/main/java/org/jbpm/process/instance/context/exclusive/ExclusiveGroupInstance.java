/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.instance.context.exclusive;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.core.context.exclusive.ExclusiveGroup;
import org.jbpm.process.instance.context.AbstractContextInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;

public class ExclusiveGroupInstance extends AbstractContextInstance {

    private static final long serialVersionUID = 510l;

    private Map<String, KogitoNodeInstance> nodeInstances = new HashMap<>();

    public String getContextType() {
        return ExclusiveGroup.EXCLUSIVE_GROUP;
    }

    public boolean containsNodeInstance(NodeInstance nodeInstance) {
        return nodeInstances.containsKey(((KogitoNodeInstance) nodeInstance).getStringId());
    }

    public void addNodeInstance(KogitoNodeInstance nodeInstance) {
        nodeInstances.put(nodeInstance.getStringId(), nodeInstance);
    }

    public Collection<KogitoNodeInstance> getNodeInstances() {
        return nodeInstances.values();
    }

}
