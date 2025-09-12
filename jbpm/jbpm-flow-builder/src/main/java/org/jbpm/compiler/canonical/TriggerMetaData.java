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
package org.jbpm.compiler.canonical;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.drools.util.StringUtils;
import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.workflow.core.node.CompositeNode;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.kogito.correlation.CompositeCorrelation;
import org.kie.kogito.internal.process.runtime.KogitoNode;

import static org.jbpm.ruleflow.core.Metadata.CORRELATION_ATTRIBUTES;
import static org.jbpm.ruleflow.core.Metadata.DATA_ONLY;
import static org.jbpm.ruleflow.core.Metadata.MAPPING_VARIABLE;
import static org.jbpm.ruleflow.core.Metadata.MESSAGE_TYPE;
import static org.jbpm.ruleflow.core.Metadata.TRIGGER_REF;
import static org.jbpm.ruleflow.core.Metadata.TRIGGER_TYPE;

public class TriggerMetaData {

    public enum TriggerType {
        ConsumeMessage,
        ProduceMessage,
        ConsumeSignal,
        ProduceSignal;

        static public boolean isSignal(TriggerType type) {
            return List.of(ConsumeSignal, ProduceSignal).contains(type);
        }

        static public boolean isMessage(TriggerType type) {
            return List.of(ConsumeMessage, ProduceMessage).contains(type);
        }

        static public boolean isConsumer(TriggerType type) {
            return List.of(ConsumeMessage, ConsumeSignal).contains(type);
        }

        static public boolean isProducer(TriggerType type) {
            return List.of(ProduceMessage, ProduceSignal).contains(type);
        }
    }

    // name of the trigger derived from message or signal
    private final String name;
    // type of the trigger e.g. message, signal, timer...
    private final TriggerType type;
    // data type of the event associated with this trigger
    private final String dataType;
    // reference in the model of the process the event should be mapped to
    private final String modelRef;
    // reference to owner of the trigger usually node
    private final String ownerId;
    // indicates if the whole event should be consumed or just the data
    private final boolean dataOnly;
    // the owner node
    private final Node node;
    // indicates if the whole event should be consumed or just the data
    private final CompositeCorrelation correlation;
    private String scope;

    public static TriggerMetaData of(Node node) {
        return of(node, (String) node.getMetaData().get(MAPPING_VARIABLE));
    }

    public static TriggerMetaData of(Node node, String mappingVariable) {
        Map<String, Object> nodeMetaData = node.getMetaData();
        return new TriggerMetaData(
                node,
                (String) nodeMetaData.get(TRIGGER_REF),
                TriggerType.valueOf((String) nodeMetaData.get(TRIGGER_TYPE)),
                (String) nodeMetaData.get(MESSAGE_TYPE),
                mappingVariable,
                getOwnerId(node),
                (Boolean) nodeMetaData.get(DATA_ONLY),
                (String) nodeMetaData.getOrDefault(Metadata.CUSTOM_SCOPE, Metadata.PROCESS_INSTANCE_SCOPE),
                (CompositeCorrelation) nodeMetaData.get(CORRELATION_ATTRIBUTES)).validate();
    }

    private TriggerMetaData(Node node, String name, TriggerType type, String dataType, String modelRef, String ownerId, Boolean dataOnly, String scope, CompositeCorrelation correlation) {
        this.node = node;
        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.modelRef = modelRef;
        this.ownerId = ownerId;
        this.dataOnly = dataOnly == null || dataOnly.booleanValue();
        this.scope = scope;
        this.correlation = correlation;
    }

    public String getName() {
        return name;
    }

    public TriggerType getType() {
        return type;
    }

    public String getDataType() {
        return dataType;
    }

    public String getModelRef() {
        return modelRef;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Node getNode() {
        return node;
    }

    public boolean dataOnly() {
        return dataOnly;
    }

    public String getScope() {
        return scope;
    }

    public CompositeCorrelation getCorrelation() {
        return correlation;
    }

    private TriggerMetaData validate() {
        if (TriggerType.isMessage(type) && (StringUtils.isEmpty(name) || StringUtils.isEmpty(dataType))) {
            throw new IllegalArgumentException("Message Trigger information is not complete " + this);
        }

        if (TriggerType.isSignal(type) && StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Signal Trigger information is not complete " + this);
        }

        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataType, modelRef, name, ownerId, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TriggerMetaData))
            return false;
        TriggerMetaData other = (TriggerMetaData) obj;
        return Objects.equals(dataType, other.dataType) && Objects.equals(modelRef, other.modelRef) && Objects.equals(
                name, other.name) && Objects.equals(ownerId, other.ownerId) && type == other.type && scope.equals(other.scope);
    }

    @Override
    public String toString() {
        return "TriggerMetaData [name=" + name + ", type=" + type + ", dataType=" + dataType + ", modelRef=" +
                modelRef + ", ownerId=" + ownerId + ", scope=" + scope + "]";
    }

    private static String getOwnerId(Node node) {
        StringBuilder prefix = new StringBuilder();
        if (node instanceof KogitoNode) {
            NodeContainer container = ((KogitoNode) node).getParentContainer();
            while (container instanceof CompositeNode) {
                CompositeNode compositeNode = (CompositeNode) container;
                prefix.append(compositeNode.getId().toSanitizeString()).append('_');
                container = compositeNode.getParentContainer();
            }
        }
        return prefix.append(node.getId().toSanitizeString()).toString();
    }

}
