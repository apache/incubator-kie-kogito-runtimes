/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.event.process;

import org.kie.kogito.event.AbstractDataEvent;

public class ProcessInstanceDataEvent<T> extends AbstractDataEvent<T> {

    public ProcessInstanceDataEvent() {
    }

    public ProcessInstanceDataEvent(T body) {
        setData(body);
    }

    public ProcessInstanceDataEvent(String type,
            String source,
            T body,
            String kogitoProcessInstanceId,
            String kogitoProcessInstanceVersion,
            String kogitoParentProcessInstanceId,
            String kogitoRootProcessInstanceId,
            String kogitoProcessId,
            String kogitoRootProcessId,
            String kogitoProcessInstanceState,
            String kogitoAddons,
            String kogitoProcessType,
            String kogitoReferenceId,
            String kogitoIdentity) {
        super(type,
                source,
                body,
                kogitoProcessInstanceId,
                kogitoRootProcessInstanceId,
                kogitoProcessId,
                kogitoRootProcessId,
                kogitoAddons,
                kogitoIdentity);
        setKogitoProcessInstanceVersion(kogitoProcessInstanceVersion);
        setKogitoParentProcessInstanceId(kogitoParentProcessInstanceId);
        setKogitoProcessInstanceState(kogitoProcessInstanceState);
        setKogitoReferenceId(kogitoReferenceId);
        setKogitoProcessType(kogitoProcessType);
    }

}
