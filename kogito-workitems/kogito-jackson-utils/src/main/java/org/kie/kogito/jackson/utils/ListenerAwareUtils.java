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

package org.kie.kogito.jackson.utils;

import java.util.Collection;

import org.kie.kogito.internal.process.event.KogitoObjectListener;
import org.kie.kogito.internal.process.event.KogitoObjectListenerAware;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

class ListenerAwareUtils {

    static void processNode(Collection<KogitoObjectListener> listeners, KogitoObjectListenerAware container, String propertyName, JsonNode oldValue, JsonNode newValue, Runnable updater) {
        if (newValue instanceof KogitoObjectListenerAware) {
            ((KogitoObjectListenerAware) newValue).addKogitoObjectListener(new InternalParentListener(container, propertyName));
        }
        listeners.forEach(l -> l.beforeValueChanged(container, propertyName, handleNull(oldValue), handleNull(newValue)));
        updater.run();
        listeners.forEach(l -> l.afterValueChanged(container, propertyName, handleNull(oldValue), handleNull(newValue)));
    }

    private static Object handleNull(Object value) {
        return value == null ? NullNode.instance : value;
    }

    private ListenerAwareUtils() {
    }
}
