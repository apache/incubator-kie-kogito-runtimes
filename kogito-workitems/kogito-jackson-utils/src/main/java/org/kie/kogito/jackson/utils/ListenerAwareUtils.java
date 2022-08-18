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

import org.kie.kogito.process.KogitoObjectListener;
import org.kie.kogito.process.KogitoObjectListenerAware;
import org.kie.kogito.process.KogitoObjectListenerFactory;

import com.fasterxml.jackson.databind.JsonNode;

class ListenerAwareUtils {

    static void processNode(Collection<KogitoObjectListener> listeners, JsonNode container, String propertyName, JsonNode oldValue, JsonNode newValue, Runnable updater) {
        listeners.forEach(l -> l.beforeValueChanged(container, propertyName, oldValue, newValue));
        if (newValue instanceof KogitoObjectListenerAware) {
            listeners.stream().filter(KogitoObjectListenerFactory.class::isInstance).map(KogitoObjectListenerFactory.class::cast)
                    .forEach(l -> ((KogitoObjectListenerAware) newValue).addKogitoObjectListener(l.newListener(propertyName)));
        }
        updater.run();
        listeners.forEach(l -> l.afterValueChanged(container, propertyName, oldValue, newValue));
    }

    private ListenerAwareUtils() {
    }
}
