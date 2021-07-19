/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.workflow.core.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.workflow.core.Connection;
import org.jbpm.workflow.core.Node;
import org.kie.kogito.process.validation.ValidationException;

/**
 * Default implementation of a connection.
 * 
 */
public class ConnectionImpl implements Connection, Serializable {

    private static final long serialVersionUID = 510l;

    private org.kie.api.definition.process.Node from;
    private org.kie.api.definition.process.Node to;
    private String fromType;
    private String toType;
    private Map<String, Object> metaData = new HashMap<String, Object>();

    public ConnectionImpl() {
    }

    /**
     * This constructor calls {@link #connect()} itself! That means
     * that simply creating the object also adds it to the appropriate
     * {@link List} fields in other objects.
     * </p>
     * Creates a new connection, given a from node, a to node
     * and a type.
     * 
     * @param from The from node
     * @param fromType The node type
     * @param to The to node
     * @param toType The connection type
     */
    public ConnectionImpl(final org.kie.api.definition.process.Node from, final String fromType,
            final org.kie.api.definition.process.Node to, final String toType) {
        if (from == null) {
            throw new IllegalArgumentException("From node is null!");
        }
        if (fromType == null) {
            throw new IllegalArgumentException("From type is null!");
        }
        if (to == null) {
            throw new IllegalArgumentException("To node is null!");
        }
        if (toType == null) {
            throw new IllegalArgumentException("To type is null!");
        }
        this.from = from;
        this.fromType = fromType;
        this.to = to;
        this.toType = toType;
        connect();
    }

    public void connect() {
        try {
            ((Node) this.from).addOutgoingConnection(fromType, this);
            ((Node) this.to).addIncomingConnection(toType, this);
        } catch (Exception exception) {
            throw new ValidationException(null, exception.getMessage());
        }
    }

    public synchronized void terminate() {
        ((Node) this.from).removeOutgoingConnection(fromType, this);
        ((Node) this.to).removeIncomingConnection(toType, this);
        this.from = null;
        this.fromType = null;
        this.to = null;
        this.toType = null;
    }

    public org.kie.api.definition.process.Node getFrom() {
        return this.from;
    }

    public org.kie.api.definition.process.Node getTo() {
        return this.to;
    }

    public String getFromType() {
        return this.fromType;
    }

    public String getToType() {
        return this.toType;
    }

    public void setFrom(org.kie.api.definition.process.Node from) {
        this.from = from;
    }

    public void setTo(org.kie.api.definition.process.Node to) {
        this.to = to;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public void setToType(String toType) {
        this.toType = toType;
    }

    public Map<String, Object> getMetaData() {
        return this.metaData;
    }

    public void setMetaData(String name, Object value) {
        this.metaData.put(name, value);
    }

    public Object getMetaData(String name) {
        return this.metaData.get(name);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("Connection ");
        sb.append(getFrom() == null ? "null" : getFrom().getName());
        sb.append(" [type=");
        sb.append(getFromType());
        sb.append("]");
        sb.append(" - ");
        sb.append(getTo() == null ? "null" : getTo().getName());
        sb.append(" [type=");
        sb.append(getToType());
        sb.append("]");
        return sb.toString();
    }

}
