/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.compiler.xml;

import org.jbpm.workflow.core.Node;
import org.kie.api.definition.process.Process;

public interface ProcessDataEventListener {

    void onNodeAdded(Node node);

    void onProcessAdded(Process process);

    void onMetaDataAdded(String name, Object data);

    void onComplete(Process process);

    void onBuildComplete(Process process);
}
