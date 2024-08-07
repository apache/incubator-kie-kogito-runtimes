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
package org.jbpm.bpmn2.xml;

import java.util.Arrays;
import java.util.List;

import org.jbpm.bpmn2.core.SequenceFlow;
import org.jbpm.compiler.xml.Parser;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.node.DynamicNode;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static org.jbpm.compiler.xml.processes.DynamicNodeHandler.AUTOCOMPLETE_COMPLETION_CONDITION;
import static org.jbpm.ruleflow.core.Metadata.COMPLETION_CONDITION;
import static org.jbpm.ruleflow.core.Metadata.CUSTOM_ACTIVATION_CONDITION;

public class AdHocSubProcessHandler extends CompositeContextNodeHandler {

    protected static final List<String> AUTOCOMPLETE_EXPRESSIONS = Arrays.asList(
            "getActivityInstanceAttribute(\"numberOfActiveInstances\") == 0", AUTOCOMPLETE_COMPLETION_CONDITION);

    @Override
    protected Node createNode(Attributes attrs) {
        DynamicNode result = new DynamicNode();
        VariableScope variableScope = new VariableScope();
        result.addContext(variableScope);
        result.setDefaultContext(variableScope);
        return result;
    }

    @Override
    public Class<?> generateNodeFor() {
        return DynamicNode.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Node handleNode(final Node node, final Element element, final String uri,
            final String localName, final Parser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        DynamicNode dynamicNode = (DynamicNode) node;
        String cancelRemainingInstances = element.getAttribute("cancelRemainingInstances");
        if ("false".equals(cancelRemainingInstances)) {
            dynamicNode.setCancelRemainingInstances(false);
        }

        dynamicNode.setLanguage("http://www.java.com/java");

        // by default it should not autocomplete as it's adhoc
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        dynamicNode.setActivationCondition((String) node.getMetaData().get(CUSTOM_ACTIVATION_CONDITION));
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if (COMPLETION_CONDITION.equals(nodeName)) {
                Element completeConditionElement = (Element) xmlNode;
                String dialect = completeConditionElement.getAttribute("language");

                String expression = xmlNode.getTextContent();
                if (AUTOCOMPLETE_EXPRESSIONS.contains(expression)) {
                    dynamicNode.setAutoComplete(true);
                } else {
                    dynamicNode.setCompletionCondition(expression);
                    if (!dialect.isBlank()) {
                        dynamicNode.setLanguage(dialect);
                    }
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
        RuleFlowProcess process = (RuleFlowProcess) ((ProcessBuildData) parser.getData()).getMetaData(ProcessHandler.CURRENT_PROCESS);
        List<SequenceFlow> connections = (List<SequenceFlow>) dynamicNode.getMetaData(ProcessHandler.CONNECTIONS);
        ProcessHandler.linkConnections(process, dynamicNode, connections);
        ProcessHandler.linkBoundaryEvents(dynamicNode);

        handleScript(dynamicNode, element, "onEntry");
        handleScript(dynamicNode, element, "onExit");
        return node;
    }

    @Override
    public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
        DynamicNode dynamicNode = (DynamicNode) node;
        writeNode("adHocSubProcess", dynamicNode, xmlDump, metaDataType);
        if (!dynamicNode.isCancelRemainingInstances()) {
            xmlDump.append(" cancelRemainingInstances=\"false\"");
        }
        xmlDump.append(" ordering=\"Parallel\" >" + EOL);
        writeExtensionElements(dynamicNode, xmlDump);
        // nodes
        List<Node> subNodes = getSubNodes(dynamicNode);
        XmlBPMNProcessDumper.INSTANCE.visitNodes(subNodes, xmlDump, metaDataType);

        // connections
        visitConnectionsAndAssociations(dynamicNode, xmlDump, metaDataType);

        if (dynamicNode.isAutoComplete()) {
            xmlDump.append("<completionCondition xsi:type=\"tFormalExpression\" language=\"" + dynamicNode.getLanguage() + "\">" + AUTOCOMPLETE_COMPLETION_CONDITION + "</completionCondition>" + EOL);
        }
        endNode("adHocSubProcess", xmlDump);
    }

}
