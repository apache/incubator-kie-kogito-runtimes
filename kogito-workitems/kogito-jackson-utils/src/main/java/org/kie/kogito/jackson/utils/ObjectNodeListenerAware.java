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
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.kie.kogito.process.KogitoObjectListener;
import org.kie.kogito.process.KogitoObjectListenerAware;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectNodeListenerAware extends ObjectNode implements KogitoObjectListenerAware {

    private static final long serialVersionUID = 1L;

    private transient Collection<KogitoObjectListener> listeners = new CopyOnWriteArrayList<>();

    public ObjectNodeListenerAware(JsonNodeFactory nc) {
        super(nc);
    }

    @Override
    protected ObjectNode _put(String fieldName, JsonNode value) {
        JsonNode oldValue = _children.put(fieldName, value);
        listeners.forEach(l -> l.onValueChanged(this, fieldName, oldValue, value));
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JsonNode> T set(String propertyName, JsonNode value) {
        final JsonNode newValue = value == null ? nullNode() : value;
        JsonNode oldValue = _children.put(propertyName, value);
        listeners.forEach(l -> l.onValueChanged(this, propertyName, oldValue, newValue));
        return (T) this;
    }

    @Override
    public JsonNode remove(String propertyName) {
        final JsonNode oldValue = super.remove(propertyName);
        listeners.forEach(l -> l.onValueChanged(this, propertyName, oldValue, nullNode()));
        return oldValue;
    }

    @Override
    public void addKogitoObjectListener(KogitoObjectListener listener) {
        listeners.add(listener);
    }

    @Override
    public ObjectNode deepCopy() {
        ObjectNodeListenerAware ret = new ObjectNodeListenerAware(_nodeFactory);
        for (Map.Entry<String, JsonNode> entry : _children.entrySet()) {
            ret._children.put(entry.getKey(), entry.getValue().deepCopy());
        }
        return ret;
    }
}
