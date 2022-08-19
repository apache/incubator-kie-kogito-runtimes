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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.process.KogitoObjectListener;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ListenerAwareTest {

    private KogitoObjectListener listener;

    @BeforeEach
    void setup() {
        listener = mock(KogitoObjectListener.class);
    }

    @Test
    void testObjectNodeChange() {
        ObjectNodeListenerAware node = (ObjectNodeListenerAware) ObjectMapperFactory.listenerAware().createObjectNode();
        node.addKogitoObjectListener(listener);
        node.put("name", "Javierito");
        verify(listener).beforeValueChanged(node, "name", null, new TextNode("Javierito"));
        verify(listener).afterValueChanged(node, "name", null, new TextNode("Javierito"));
        node.put("name", 3);
        verify(listener).beforeValueChanged(node, "name", new TextNode("Javierito"), new IntNode(3));
        verify(listener).afterValueChanged(node, "name", new TextNode("Javierito"), new IntNode(3));
        node.remove("name");
        verify(listener).beforeValueChanged(node, "name", new IntNode(3), null);
        verify(listener).afterValueChanged(node, "name", new IntNode(3), null);
    }

    @Test
    void testArrayNodeChange() {
        ArrayNodeListenerAware node = (ArrayNodeListenerAware) ObjectMapperFactory.listenerAware().createArrayNode();
        node.addKogitoObjectListener(listener);
        node.add("Javierito");
        verify(listener).beforeValueChanged(node, "[0]", null, new TextNode("Javierito"));
        verify(listener).afterValueChanged(node, "[0]", null, new TextNode("Javierito"));
        node.insert(1, "OtherJavierito");
        verify(listener).beforeValueChanged(node, "[1]", null, new TextNode("OtherJavierito"));
        verify(listener).afterValueChanged(node, "[1]", null, new TextNode("OtherJavierito"));
        node.set(1, "NotLikeJavierito");
        verify(listener).beforeValueChanged(node, "[1]", new TextNode("OtherJavierito"), new TextNode("NotLikeJavierito"));
        verify(listener).afterValueChanged(node, "[1]", new TextNode("OtherJavierito"), new TextNode("NotLikeJavierito"));
        node.remove(0);
        verify(listener).beforeValueChanged(node, "[0]", new TextNode("Javierito"), null);
        verify(listener).afterValueChanged(node, "[0]", new TextNode("Javierito"), null);
    }

    @Test
    void testComplexObjectNodeChange() {
        ObjectNodeListenerAware node = (ObjectNodeListenerAware) ObjectMapperFactory.listenerAware().createObjectNode();
        node.addKogitoObjectListener(listener);
        ObjectNode embedded = node.objectNode().put("surname", "Javierito");
        node.set("name", embedded);
        verify(listener).beforeValueChanged(node, "name", null, embedded);
        verify(listener).afterValueChanged(node, "name", null, embedded);
        embedded.put("surname", "NotLikeJavierito");
        verify(listener).beforeValueChanged(node, "name.surname", new TextNode("Javierito"), new TextNode("NotLikeJavierito"));
        verify(listener).afterValueChanged(node, "name.surname", new TextNode("Javierito"), new TextNode("NotLikeJavierito"));
    }

}
