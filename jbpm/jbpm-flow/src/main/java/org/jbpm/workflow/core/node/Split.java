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
package org.jbpm.workflow.core.node;

import java.util.Collections;
import java.util.Map;

import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionRef;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.kie.api.definition.process.Connection;

/**
 * Default implementation of a split node.
 * 
 */
public class Split extends NodeImpl implements Constrainable {

    public static final int TYPE_UNDEFINED = 0;
    /**
     * All outgoing connections of a split of this type are triggered
     * when its incoming connection has been triggered. A split of this
     * type should have no constraints linked to any of its outgoing
     * connections.
     */
    public static final int TYPE_AND = 1;
    /**
     * Exactly one outgoing connection of a split of this type is triggered
     * when its incoming connection has been triggered. Which connection
     * is based on the constraints associated with each of the connections:
     * the connection with the highest priority whose constraint is satisfied
     * is triggered.
     */
    public static final int TYPE_XOR = 2;
    /**
     * One or multiple outgoing connections of a split of this type are
     * triggered when its incoming connection has been triggered. Which
     * connections is based on the constraints associated with each of the
     * connections: all connections whose constraint is satisfied are
     * triggered.
     */
    public static final int TYPE_OR = 3;
    public static final int TYPE_XAND = 4;

    private static final long serialVersionUID = 510l;

    private int type;

    public Split() {
        this.type = TYPE_UNDEFINED;
    }

    public Split(final int type) {
        this.type = type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public boolean isDefault(final Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection is null");
        }

        if (this.type == TYPE_OR || this.type == TYPE_XOR) {
            ConnectionRef ref = new ConnectionRef((String) connection.getMetaData().get("UniqueId"), connection.getTo().getId(), connection.getToType());
            Constraint constraint = this.constraints.get(ref);
            String defaultConnection = (String) getMetaData().get("Default");
            String connectionId = (String) connection.getMetaData().get("UniqueId");
            if (constraint != null) {
                return constraint.isDefault();
            } else if (constraint == null && connectionId.equals(defaultConnection)) {
                return true;
            } else {
                return false;
            }
        }
        throw new UnsupportedOperationException("Constraints are " +
                "only supported with XOR or OR split types, not with: " + getType());
    }

    @Override
    public Constraint getConstraint(final Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection is null");
        }

        if (this.type == TYPE_OR || this.type == TYPE_XOR) {
            ConnectionRef ref = new ConnectionRef((String) connection.getMetaData().get("UniqueId"), connection.getTo().getId(), connection.getToType());
            return this.constraints.get(ref);
        }
        throw new UnsupportedOperationException("Constraints are " +
                "only supported with XOR or OR split types, not with: " + getType());
    }

    @Override
    public Constraint internalGetConstraint(final ConnectionRef ref) {
        return this.constraints.get(ref);
    }

    @Override
    public void setConstraint(final Connection connection,
            final Constraint constraint) {
        if (this.type == TYPE_OR || this.type == TYPE_XOR) {
            if (connection == null) {
                throw new IllegalArgumentException("connection is null");
            }
            if (!getDefaultOutgoingConnections().contains(connection)) {
                throw new IllegalArgumentException("connection is unknown:" + connection);
            }
            addConstraint(
                    new ConnectionRef((String) connection.getMetaData().get("UniqueId"), connection.getTo().getId(), connection.getToType()),
                    constraint);
        } else {
            throw new UnsupportedOperationException("Constraints are " +
                    "only supported with XOR or OR split types, not with type:" + getType());
        }
    }

    @Override
    public void addConstraint(ConnectionRef connectionRef, Constraint constraint) {
        if (connectionRef == null) {
            throw new IllegalArgumentException(
                    "A split node only accepts constraints linked to a connection");
        }
        this.constraints.put(connectionRef, constraint);
    }

    @Override
    public Map<ConnectionRef, Constraint> getConstraints() {
        return Collections.unmodifiableMap(this.constraints);
    }

    @Override
    public void validateAddIncomingConnection(final String type, final Connection connection) {
        super.validateAddIncomingConnection(type, connection);
        if (!Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
            throw new IllegalArgumentException(
                    "This type of node [" + connection.getTo().getMetaData().get("UniqueId") + ", " + connection.getTo().getName()
                            + "] only accepts default incoming connection type!");
        }

        if (!getIncomingConnections(Node.CONNECTION_DEFAULT_TYPE).isEmpty() && !"true".equals(System.getProperty("jbpm.enable.multi.con"))) {
            throw new IllegalArgumentException(
                    "This type of node [" + connection.getTo().getMetaData().get("UniqueId") + ", " + connection.getTo().getName()
                            + "] cannot have more than one incoming connection!");
        }
    }

    @Override
    public void validateAddOutgoingConnection(final String type, final Connection connection) {
        super.validateAddOutgoingConnection(type, connection);
        if (!Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
            throw new IllegalArgumentException(
                    "This type of node [" + connection.getFrom().getMetaData().get("UniqueId") + ", " + connection.getFrom().getName()
                            + "] only accepts default outgoing connection type!");
        }
    }

    @Override
    public void removeOutgoingConnection(final String type, final Connection connection) {
        super.removeOutgoingConnection(type, connection);
        removeConstraint(connection);
    }

    public void removeConstraint(Connection connection) {
        ConnectionRef ref = new ConnectionRef((String) connection.getMetaData().get("UniqueId"), connection.getTo().getId(), connection.getToType());
        internalRemoveConstraint(ref);
    }

    public void internalRemoveConstraint(ConnectionRef ref) {
        this.constraints.remove(ref);
    }

}
