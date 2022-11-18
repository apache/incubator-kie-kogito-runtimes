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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jbpm.compiler.xml.Handler;
import org.jbpm.compiler.xml.Parser;
import org.jbpm.compiler.xml.compiler.XmlDumper;
import org.jbpm.compiler.xml.core.BaseAbstractHandler;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class AbstractNodeHandler extends BaseAbstractHandler implements Handler {

    protected static final String EOL = System.getProperty("line.separator");

    public AbstractNodeHandler() {
        initValidParents();
        initValidPeers();
        this.allowNesting = false;
    }

    protected void initValidParents() {
        this.validParents = new HashSet<Class<?>>();
        this.validParents.add(NodeContainer.class);
    }

    protected void initValidPeers() {
        this.validPeers = new HashSet<Class<?>>();
        this.validPeers.add(null);
        this.validPeers.add(Node.class);
    }

    public Object start(final String uri, final String localName, final Attributes attrs,
            final Parser parser) throws SAXException {
        parser.startElementBuilder(localName,
                attrs);

        NodeContainer nodeContainer = (NodeContainer) parser.getParent();

        final Node node = createNode();

        final String id = attrs.getValue("id");
        node.setId(Long.valueOf(id));

        final String name = attrs.getValue("name");
        node.setName(name);

        nodeContainer.addNode(node);

        return node;
    }

    protected abstract Node createNode();

    public Object end(final String uri, final String localName,
            final Parser parser) throws SAXException {
        final Element element = parser.endElementBuilder();
        Node node = (Node) parser.getCurrent();
        handleNode(node, element, uri, localName, parser);
        return node;
    }

    protected void handleNode(final Node node, final Element element, final String uri,
            final String localName, final Parser parser) throws SAXException {
        final String x = element.getAttribute("x");
        if (x != null && x.length() != 0) {
            try {
                node.setMetaData("x", Integer.valueOf(x));
            } catch (NumberFormatException exc) {
                throw new SAXParseException("<" + localName + "> requires an Integer 'x' attribute", parser.getLocator());
            }
        }
        final String y = element.getAttribute("y");
        if (y != null && y.length() != 0) {
            try {
                node.setMetaData("y", Integer.valueOf(y));
            } catch (NumberFormatException exc) {
                throw new SAXParseException("<" + localName + "> requires an Integer 'y' attribute", parser.getLocator());
            }
        }
        final String width = element.getAttribute("width");
        if (width != null && width.length() != 0) {
            try {
                node.setMetaData("width", Integer.valueOf(width));
            } catch (NumberFormatException exc) {
                throw new SAXParseException("<" + localName + "> requires an Integer 'width' attribute", parser.getLocator());
            }
        }
        final String height = element.getAttribute("height");
        if (height != null && height.length() != 0) {
            try {
                node.setMetaData("height", Integer.valueOf(height));
            } catch (NumberFormatException exc) {
                throw new SAXParseException("<" + localName + "> requires an Integer 'height' attribute", parser.getLocator());
            }
        }
        final String color = element.getAttribute("color");
        if (color != null && color.length() != 0) {
            try {
                node.setMetaData("color", Integer.valueOf(color));
            } catch (NumberFormatException exc) {
                throw new SAXParseException("<" + localName + "> requires an Integer 'color' attribute", parser.getLocator());
            }
        }
    }

    protected void handleAction(final ExtendedNodeImpl node, final Element element, String type) {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Node xmlNode = nodeList.item(i);
            String nodeName = xmlNode.getNodeName();
            if (nodeName.equals(type)) {
                List<DroolsAction> actions = new ArrayList<>();
                NodeList subNodeList = xmlNode.getChildNodes();
                for (int j = 0; j < subNodeList.getLength(); j++) {
                    Element subXmlNode = (Element) subNodeList.item(j);
                    DroolsAction action = extractAction(subXmlNode);
                    actions.add(action);
                }
                node.setActions(type, actions);
                return;
            }
        }
    }

    public static DroolsAction extractAction(Element xmlNode) {
        String actionType = xmlNode.getAttribute("type");
        if ("expression".equals(actionType)) {
            String consequence = xmlNode.getTextContent();
            return new DroolsConsequenceAction(xmlNode.getAttribute("dialect"), consequence);
        } else {
            throw new IllegalArgumentException(
                    "Unknown action type " + actionType);
        }
    }

    public abstract void writeNode(final Node node, final StringBuilder xmlDump, final boolean includeMeta);

    protected void writeNode(final String name, final Node node, final StringBuilder xmlDump, final boolean includeMeta) {
        xmlDump.append("    <" + name + " id=\"" + node.getId() + "\" ");
        if (node.getName() != null) {
            xmlDump.append("name=\"" + XmlDumper.replaceIllegalChars(node.getName()) + "\" ");
        }
        if (includeMeta) {
            Integer x = (Integer) node.getMetaData().get("x");
            Integer y = (Integer) node.getMetaData().get("y");
            Integer width = (Integer) node.getMetaData().get("width");
            Integer height = (Integer) node.getMetaData().get("height");
            Integer color = (Integer) node.getMetaData().get("color");
            if (x != null && x != 0) {
                xmlDump.append("x=\"" + x + "\" ");
            }
            if (y != null && y != 0) {
                xmlDump.append("y=\"" + y + "\" ");
            }
            if (width != null && width != -1) {
                xmlDump.append("width=\"" + width + "\" ");
            }
            if (height != null && height != -1) {
                xmlDump.append("height=\"" + height + "\" ");
            }
            if (color != null && color != 0) {
                xmlDump.append("color=\"" + color + "\" ");
            }
        }
    }

    protected boolean containsMetaData(final Node node) {
        for (Map.Entry<String, Object> entry : ((NodeImpl) node).getMetaData().entrySet()) {
            String name = entry.getKey();
            if (!"x".equals(name)
                    && !"y".equals(name)
                    && !"width".equals(name)
                    && !"height".equals(name)
                    && !"color".equals(name)
                    && !"UniqueId".equals(name)
                    && entry.getValue() instanceof String) {
                return true;
            }
        }
        return false;
    }

    protected void writeMetaData(final Node node, final StringBuilder xmlDump) {
        for (Map.Entry<String, Object> entry : ((NodeImpl) node).getMetaData().entrySet()) {
            String name = entry.getKey();
            if (!"x".equals(name)
                    && !"y".equals(name)
                    && !"width".equals(name)
                    && !"height".equals(name)
                    && !"color".equals(name)
                    && entry.getValue() instanceof String) {
                xmlDump.append("      <metaData name=\"" + name + "\">" + EOL);
                xmlDump.append("        <value>" + entry.getValue() + "</value>" + EOL);
                xmlDump.append("      </metaData>" + EOL);
            }
        }
    }

    protected void writeActions(final String type, List<DroolsAction> actions, final StringBuilder xmlDump) {
        if (actions != null && !actions.isEmpty()) {
            xmlDump.append("      <" + type + ">" + EOL);
            for (DroolsAction action : actions) {
                writeAction(action, xmlDump);
            }
            xmlDump.append("      </" + type + ">" + EOL);
        }
    }

    public static void writeAction(final DroolsAction action, final StringBuilder xmlDump) {
        if (action instanceof DroolsConsequenceAction) {
            DroolsConsequenceAction consequenceAction = (DroolsConsequenceAction) action;
            xmlDump.append("        <action type=\"expression\" ");
            String name = consequenceAction.getName();
            if (name != null) {
                xmlDump.append("name=\"" + name + "\" ");
            }
            String dialect = consequenceAction.getDialect();
            if (dialect != null) {
                xmlDump.append("dialect=\"" + dialect + "\" ");
            }
            String consequence = consequenceAction.getConsequence();
            if (consequence == null) {
                xmlDump.append("/>" + EOL);
            } else {
                xmlDump.append(">" + XmlDumper.replaceIllegalChars(consequence.trim()) + "</action>" + EOL);
            }
        } else {
            throw new IllegalArgumentException(
                    "Unknown action " + action);
        }
    }

    public void writeTimers(final Map<Timer, DroolsAction> timers, final StringBuilder xmlDump) {
        if (timers != null && !timers.isEmpty()) {
            xmlDump.append("      <timers>" + EOL);
            List<Timer> timerList = new ArrayList<>(timers.keySet());
            Collections.sort(timerList, new Comparator<Timer>() {
                public int compare(Timer o1, Timer o2) {
                    return (int) (o2.getId() - o1.getId());
                }
            });
            for (Timer timer : timerList) {
                xmlDump.append("        <timer id=\"" + timer.getId() + "\" ");
                if (timer.getDelay() != null) {
                    xmlDump.append("delay=\"" + timer.getDelay() + "\" ");
                }
                if (timer.getPeriod() != null) {
                    xmlDump.append("period=\"" + timer.getPeriod() + "\" ");
                }
                xmlDump.append(">" + EOL);
                writeAction(timers.get(timer), xmlDump);
                xmlDump.append("        </timer>" + EOL);
            }
            xmlDump.append("      </timers>" + EOL);
        }
    }

    protected void endNode(final StringBuilder xmlDump) {
        xmlDump.append("/>" + EOL);
    }

    protected void endNode(final String name, final StringBuilder xmlDump) {
        xmlDump.append("    </" + name + ">" + EOL);
    }

}
