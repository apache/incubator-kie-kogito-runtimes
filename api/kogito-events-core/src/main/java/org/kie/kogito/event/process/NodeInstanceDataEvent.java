/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.event.process;

import java.util.Map;

public class NodeInstanceDataEvent extends ProcessDataEvent<NodeInstanceEventBody> {

    public NodeInstanceDataEvent() {
    }

    public NodeInstanceDataEvent(String source, String addons, Map<String, Object> metaData, NodeInstanceEventBody body) {
        super("NodeInstanceEvent",
                source,
                body,
                (String) metaData.get(ProcessInstanceEventBody.ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventBody.VERSION_META_DATA),
                (String) metaData.get(ProcessInstanceEventBody.PARENT_ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventBody.ROOT_ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventBody.PROCESS_ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventBody.ROOT_PROCESS_ID_META_DATA),
                (String) metaData.get(ProcessInstanceEventBody.STATE_META_DATA),
                addons,
                (String) metaData.get(ProcessInstanceEventBody.PROCESS_TYPE_META_DATA),
                null);
    }
}
