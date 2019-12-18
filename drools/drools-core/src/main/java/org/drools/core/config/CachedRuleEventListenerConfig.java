/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.config;

import java.util.ArrayList;
import java.util.Collection;

import org.kie.kogito.rules.RuleEventListenerConfig;
import org.kie.kogito.rules.listeners.AgendaListener;
import org.kie.kogito.rules.listeners.DataSourceListener;

public class CachedRuleEventListenerConfig implements RuleEventListenerConfig {

    private Collection<AgendaListener> agendaListener = new ArrayList<>();
    private Collection<DataSourceListener> dataSourceListener = new ArrayList<>();

    public CachedRuleEventListenerConfig register(AgendaListener listener) {
        agendaListener.add(listener);
        return this;
    }

    public CachedRuleEventListenerConfig register(DataSourceListener listener) {
        dataSourceListener.add(listener);
        return this;
    }

    public Collection<AgendaListener> agendaListener() {
        return agendaListener;
    }

    public Collection<DataSourceListener> dataSourceListener() {
        return dataSourceListener;
    }
}
