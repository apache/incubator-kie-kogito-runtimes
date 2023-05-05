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
package org.jbpm.bpmn2.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jbpm.compiler.xml.Parser;
import org.jbpm.process.core.Work;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class UserTaskHandler extends TaskHandler {

    @Override
    protected Node createNode(Attributes attrs) {
        return new HumanTaskNode();
    }

    @Override
    public Class<HumanTaskNode> generateNodeFor() {
        return HumanTaskNode.class;
    }

    private static final Set<String> taskParameters = Set.of(
            "NotStartedNotify", "NotCompletedNotify", "NotCompletedReassign", "NotStartedReassign", "Description", "Comment", "ActorId", "GroupId", "Priority", "Skippable", "Content");

    @Override
    protected Node handleNode(final Node node, final Element element, final String uri,
            final String localName, final Parser parser) throws SAXException {
        Node currentNode = super.handleNode(node, element, uri, localName, parser);
        HumanTaskNode humanTaskNode = (HumanTaskNode) node;
        Work work = humanTaskNode.getWork();
        work.setName("Human Task");

        taskParameters.forEach(p -> setParameter(work, p, humanTaskNode.getIoSpecification().getDataInputAssociation()));

        List<String> owners = new ArrayList<>();
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            // ioSpec and data{Input,Output}Spec handled in super.handleNode(...)
            if ("potentialOwner".equals(nodeName)) {
                String owner = readPotentialOwner(xmlNode, humanTaskNode);
                if (owner != null) {
                    owners.add(owner);
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
        if (!owners.isEmpty()) {
            StringBuilder owner = new StringBuilder(owners.get(0));
            for (int i = 1; i < owners.size(); i++) {
                owner.append(",").append(owners.get(i));
            }
            humanTaskNode.getWork().setParameter("ActorId", owner.toString());
        }

        return currentNode;
    }

    @Override
    public Object end(String uri, String localName, Parser parser) throws SAXException {
        return super.end(uri, localName, parser);
    }

    protected String readPotentialOwner(org.w3c.dom.Node xmlNode, HumanTaskNode humanTaskNode) {
        org.w3c.dom.Node node = xmlNode.getFirstChild();
        if (node != null) {
            node = node.getFirstChild();
            if (node != null) {
                node = node.getFirstChild();
                if (node != null) {
                    return node.getTextContent();
                }
            }
        }
        return null;
    }

    @Override
    public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
        HumanTaskNode humanTaskNode = (HumanTaskNode) node;
        writeNode("userTask", humanTaskNode, xmlDump, metaDataType);
        xmlDump.append(">" + EOL);
        writeExtensionElements(humanTaskNode, xmlDump);
        writeIO(humanTaskNode.getIoSpecification(), xmlDump);
        writeMultiInstance(humanTaskNode.getMultiInstanceSpecification(), xmlDump);
        String ownerString = (String) humanTaskNode.getWork().getParameter("ActorId");
        if (ownerString != null) {
            String[] owners = ownerString.split(",");
            for (String owner : owners) {
                xmlDump.append(
                        "      <potentialOwner>" + EOL +
                                "        <resourceAssignmentExpression>" + EOL +
                                "          <formalExpression>" + owner + "</formalExpression>" + EOL +
                                "        </resourceAssignmentExpression>" + EOL +
                                "      </potentialOwner>" + EOL);
            }
        }
        endNode("userTask", xmlDump);
    }

}
