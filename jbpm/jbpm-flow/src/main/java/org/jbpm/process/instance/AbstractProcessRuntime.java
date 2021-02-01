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

package org.jbpm.process.instance;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.event.KogitoProcessEventSupport;
import org.drools.core.event.ProcessEventSupport;
import org.jbpm.process.instance.event.KogitoProcessEventListenerAdapter;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.kogito.process.event.KogitoProcessEventListener;

public abstract class AbstractProcessRuntime implements InternalProcessRuntime {

    protected KogitoProcessEventSupport processEventSupport;

    private final Map<ProcessEventListener, KogitoProcessEventListener> listenersMap = new IdentityHashMap<>();

    public KogitoProcessEventSupport getProcessEventSupport() {
        return processEventSupport;
    }

    public void setProcessEventSupport( ProcessEventSupport processEventSupport) {
        throw new UnsupportedOperationException();
    }

    public void addEventListener(final ProcessEventListener listener) {
        this.processEventSupport.addEventListener( asKogitoProcessEventListener( listener ) );
    }

    public void removeEventListener(final ProcessEventListener listener) {
        this.processEventSupport.removeEventListener( removeKogitoProcessEventListener( listener ) );
    }

    public List<ProcessEventListener> getProcessEventListeners() {
        return (List<ProcessEventListener>) (Object) processEventSupport.getEventListeners();
    }


    private KogitoProcessEventListener asKogitoProcessEventListener( ProcessEventListener processEventListener) {
        if (processEventListener instanceof KogitoProcessEventListener) {
            return (( KogitoProcessEventListener ) processEventListener);
        }
        return listenersMap.computeIfAbsent( processEventListener, KogitoProcessEventListenerAdapter::new );
    }

    private KogitoProcessEventListener removeKogitoProcessEventListener( ProcessEventListener processEventListener) {
        if (processEventListener instanceof KogitoProcessEventListener) {
            return (( KogitoProcessEventListener ) processEventListener);
        }
        return listenersMap.remove( processEventListener );
    }
}
