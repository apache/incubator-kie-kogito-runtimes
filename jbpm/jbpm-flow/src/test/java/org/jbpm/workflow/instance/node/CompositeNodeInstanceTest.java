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
package org.jbpm.workflow.instance.node;

import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.process.NodeInstance;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeNodeInstanceTest {

    private CompositeNodeInstance compositeNodeInstance;

    @BeforeEach
    public void init() {
        compositeNodeInstance = new CheckedCompositeNodeInstance();
    }

    @Test
    void addNodeInstanceSerializable() {
        SerializableNode serializableNode = new SerializableNode();
        compositeNodeInstance.addNodeInstance(serializableNode);
        Collection<NodeInstance> nodeInstances = compositeNodeInstance.getNodeInstances();
        assertThat(nodeInstances).isNotNull().hasSize(1).containsExactly(serializableNode);
        Collection<NodeInstance> serializableNodeInstances = compositeNodeInstance.getSerializableNodeInstances();
        assertThat(serializableNodeInstances).isNotNull().hasSize(1).containsExactly(serializableNode);
    }

    @Test
    void addNodeInstanceNotSerializable() {
        NotSerializableNode notSerializableNode = new NotSerializableNode();
        compositeNodeInstance.addNodeInstance(notSerializableNode);
        Collection<NodeInstance> nodeInstances = compositeNodeInstance.getNodeInstances();
        assertThat(nodeInstances).isNotNull().hasSize(1).containsExactly(notSerializableNode);
        Collection<NodeInstance> serializableNodeInstances = compositeNodeInstance.getSerializableNodeInstances();
        assertThat(serializableNodeInstances).isNotNull().isEmpty();
    }

    @Test
    void removeNodeInstance() {
        SerializableNode serializableNode = new SerializableNode();
        compositeNodeInstance.addNodeInstance(serializableNode);
        NotSerializableNode notSerializableNode = new NotSerializableNode();
        compositeNodeInstance.addNodeInstance(notSerializableNode);
        Collection<NodeInstance> nodeInstances = compositeNodeInstance.getNodeInstances();
        assertThat(nodeInstances).isNotNull().hasSize(2).containsExactly(serializableNode, notSerializableNode);
        Collection<NodeInstance> serializableNodeInstances = compositeNodeInstance.getSerializableNodeInstances();
        assertThat(serializableNodeInstances).isNotNull().hasSize(1).containsExactly(serializableNode);

        compositeNodeInstance.removeNodeInstance(serializableNode);
        nodeInstances = compositeNodeInstance.getNodeInstances();
        assertThat(nodeInstances).isNotNull().hasSize(1).containsExactly(notSerializableNode);
        serializableNodeInstances = compositeNodeInstance.getSerializableNodeInstances();
        assertThat(serializableNodeInstances).isNotNull().isEmpty();

        compositeNodeInstance.removeNodeInstance(notSerializableNode);
        nodeInstances = compositeNodeInstance.getNodeInstances();
        assertThat(nodeInstances).isNotNull().isEmpty();
        serializableNodeInstances = compositeNodeInstance.getSerializableNodeInstances();
        assertThat(serializableNodeInstances).isNotNull().isEmpty();
    }

    private static class CheckedCompositeNodeInstance extends CompositeNodeInstance {

        private static final Set<Class<? extends org.kie.api.runtime.process.NodeInstance>> notSerializableClasses = Set.of(NotSerializableNode.class);

        @Override
        protected Set<Class<? extends org.kie.api.runtime.process.NodeInstance>> getNotSerializableClasses() {
            return notSerializableClasses;
        }
    }

    private static class SerializableNode extends CompositeNodeInstance { }

    private static class NotSerializableNode extends CompositeNodeInstance { }
}