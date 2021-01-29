/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.process.runtime;

import java.util.Map;

import org.kie.api.definition.process.Process;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.process.event.KogitoEventListener;

public interface KogitoProcessInstance extends ProcessInstance, KogitoEventListener {

    int STATE_ERROR = 5;

    @Deprecated
    @Override
    long getId();

    String getStringId();

    @Deprecated
    @Override
    long getParentProcessInstanceId();

    /**
     * Returns parent process instance id if this process instance has a parent
     * @return the unique id of parent process instance, null if this process instance doesn't have a parent
     */
    String getParentProcessInstanceStringId();

    /**
     * Returns root process instance id if this process instance has a root process instance
     * @return the unique id of root process instance, null if this process instance doesn't have a root or is a root itself
     */
    String getRootProcessInstanceId();

    /**
     * The id of the root process definition that is related to this process instance.
     * @return the id of the root process definition that is related to this process instance
     */
    String getRootProcessId();

    /**
     * Returns current snapshot of process instance variables
     * @return non empty map of process instance variables
     */
    Map<String, Object> getVariables();

    /**
     * Returns optional reference id this process instance was triggered by
     * @return reference id or null if not set
     */
    String getReferenceId();

    /**
     * The description of the current process instance
     * @return the process instance description
     */
    String getDescription();

    static KogitoProcessInstance adapt(ProcessInstance processInstance) {
        return processInstance instanceof KogitoProcessInstance ?
                ( KogitoProcessInstance ) processInstance :
                new KogitoProcessInstanceAdapter( processInstance );
    }

    class KogitoProcessInstanceAdapter implements KogitoProcessInstance {

        private final ProcessInstance delegate;

        public KogitoProcessInstanceAdapter( ProcessInstance delegate ) {
            this.delegate = delegate;
        }

        @Override
        public String getProcessId() {
            return delegate.getProcessId();
        }

        @Override
        public Process getProcess() {
            return delegate.getProcess();
        }

        @Override
        public long getId() {
            return delegate.getId();
        }

        @Override
        public String getProcessName() {
            return delegate.getProcessName();
        }

        @Override
        public int getState() {
            return delegate.getState();
        }

        @Override
        public long getParentProcessInstanceId() {
            return delegate.getParentProcessInstanceId();
        }

        @Override
        public void signalEvent( String s, Object o ) {
            delegate.signalEvent( s, o );
        }

        @Override
        public String[] getEventTypes() {
            return delegate.getEventTypes();
        }

        @Override
        public String getStringId() {
            return "" + getId();
        }

        @Override
        public String getParentProcessInstanceStringId() {
            return "" + getParentProcessInstanceId();
        }

        @Override
        public String getRootProcessInstanceId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRootProcessId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> getVariables() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getReferenceId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException();
        }
    }
}
