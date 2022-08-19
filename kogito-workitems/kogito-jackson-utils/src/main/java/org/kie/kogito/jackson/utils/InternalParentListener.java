/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.jackson.utils;

import org.kie.kogito.process.KogitoObjectListener;
import org.kie.kogito.process.KogitoObjectListenerAware;

class InternalParentListener implements KogitoObjectListener {

    private final KogitoObjectListenerAware container;
    private final String prefix;

    public InternalParentListener(KogitoObjectListenerAware container, String prefix) {
        this.container = container;
        this.prefix = prefix;
    }

    private String concat(String property) {
        return prefix.isBlank() ? property : prefix + "." + property;
    }

    @Override
    public void beforeValueChanged(Object node, String property, Object oldValue, Object newValue) {
        container.listeners().forEach(l -> l.beforeValueChanged(container, concat(property), oldValue, newValue));
    }

    @Override
    public void afterValueChanged(Object node, String property, Object oldValue, Object newValue) {
        container.listeners().forEach(l -> l.afterValueChanged(container, concat(property), oldValue, newValue));
    }
}
