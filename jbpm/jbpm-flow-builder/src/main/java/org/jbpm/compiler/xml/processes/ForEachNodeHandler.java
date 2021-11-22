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
package org.jbpm.compiler.xml.processes;

import java.util.List;
import java.util.Map;

import org.drools.compiler.compiler.xml.XmlDumper;
import org.drools.core.xml.ExtensibleXmlParser;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.kie.api.definition.process.Connection;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ForEachNodeHandler extends CompositeNodeHandler {

    protected Node createNode() {
        return new ForEachNode();
    }

    public Class generateNodeFor() {
        return ForEachNode.class;
    }

    protected String getNodeName() {
        return "forEach";
    }

    protected void writeAttributes(CompositeNode compositeNode, StringBuilder xmlDump, boolean includeMeta) {
        ForEachNode forEachNode = (ForEachNode) compositeNode;
        String variableName = forEachNode.getVariableName();
        if (variableName != null) {
            xmlDump.append("variableName=\"" + variableName + "\" ");
        }
        String collectionExpression = forEachNode.getCollectionExpression();
        if (collectionExpression != null) {
            xmlDump.append("collectionExpression=\"" + XmlDumper.replaceIllegalChars(collectionExpression) + "\" ");
        }
        boolean waitForCompletion = forEachNode.isWaitForCompletion();
        if (!waitForCompletion) {
            xmlDump.append("waitForCompletion=\"false\" ");
        }
    }

    protected List<Node> getSubNodes(CompositeNode compositeNode) {
        return super.getSubNodes(((ForEachNode) compositeNode).getCompositeNode());
    }

    protected List<Connection> getSubConnections(CompositeNode compositeNode) {
        return super.getSubConnections(((ForEachNode) compositeNode).getCompositeNode());
    }

    protected Map<String, CompositeNode.NodeAndType> getInPorts(CompositeNode compositeNode) {
        return ((ForEachNode) compositeNode).getCompositeNode().getLinkedIncomingNodes();
    }

    protected Map<String, CompositeNode.NodeAndType> getOutPorts(CompositeNode compositeNode) {
        return ((ForEachNode) compositeNode).getCompositeNode().getLinkedOutgoingNodes();
    }

    protected void handleNode(final Node node, final Element element, final String uri,
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        ForEachNode forEachNode = (ForEachNode) node;
        final String variableName = element.getAttribute("variableName");
        if (variableName != null && variableName.length() != 0) {
            forEachNode.setVariable(variableName, new ObjectDataType());
        }
        final String collectionExpression = element.getAttribute("collectionExpression");
        if (collectionExpression != null && collectionExpression.length() != 0) {
            forEachNode.setCollectionExpression(collectionExpression);
        }
        final String waitForCompletion = element.getAttribute("waitForCompletion");
        if ("false".equals(waitForCompletion)) {
            forEachNode.setWaitForCompletion(false);
        }
        final String isSequential = element.getAttribute("isSequential");
        if ("false".equals(isSequential)) {
            forEachNode.setSequential(false);
        }
    }
}
