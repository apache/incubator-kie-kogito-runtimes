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
import java.util.concurrent.CopyOnWriteArrayList;

import org.kie.kogito.process.KogitoObjectListener;
import org.kie.kogito.process.KogitoObjectListenerAware;
import org.kie.kogito.process.KogitoObjectListenerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class ArrayNodeListenerAware extends ArrayNode implements KogitoObjectListenerAware {

    private static final long serialVersionUID = 1L;

    private transient Collection<KogitoObjectListener> listeners = new CopyOnWriteArrayList<>();

    public ArrayNodeListenerAware(JsonNodeFactory nf) {
        super(nf);
    }

    public ArrayNodeListenerAware(JsonNodeFactory nf, int capacity) {
        super(nf, capacity);
    }

    @Override
    public void addKogitoObjectListener(KogitoObjectListener listener) {
        listeners.add(listener);

    }

    @Override
    protected ArrayNode _set(int index, JsonNode node) {
        JsonNode oldValue = super.get(index);
        super._set(index, node);
        processNode(index, oldValue, node);
        return this;
    }

    private void processNode(int index, JsonNode oldValue, JsonNode newValue) {
        String propertyName = "[" + index + "]";
        if (newValue instanceof KogitoObjectListenerAware) {
            listeners.stream().filter(KogitoObjectListenerFactory.class::isInstance).map(KogitoObjectListenerFactory.class::cast)
                    .forEach(l -> ((KogitoObjectListenerAware) newValue).addKogitoObjectListener(l.newListener(propertyName)));
        }
        listeners.forEach(l -> l.onValueChanged(this, propertyName, oldValue, newValue));
    }

    @Override
    protected ArrayNode _add(JsonNode node) {
        super._add(node);
        processNode(size() - 1, null, node);
        return this;
    }

    @Override
    protected ArrayNode _insert(int index, JsonNode node) {
        super._insert(index, node);
        processNode(index, null, node);
        return this;
    }

    @Override
    public Collection<KogitoObjectListener> listeners() {
        return listeners;
    }
}
