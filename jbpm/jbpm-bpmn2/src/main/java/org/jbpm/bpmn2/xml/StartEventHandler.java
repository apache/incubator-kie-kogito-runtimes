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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.bpmn2.core.Error;
import org.jbpm.bpmn2.core.Escalation;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.bpmn2.core.Signal;
import org.jbpm.compiler.canonical.TriggerMetaData;
import org.jbpm.compiler.xml.Parser;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.compiler.xml.compiler.XmlDumper;
import org.jbpm.process.core.correlation.CorrelationManager;
import org.jbpm.process.core.event.EventFilter;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.process.core.event.NonAcceptingEventTypeFilter;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.ConstraintTrigger;
import org.jbpm.workflow.core.node.EventSubProcessNode;
import org.jbpm.workflow.core.node.EventTrigger;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.Trigger;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static org.jbpm.ruleflow.core.Metadata.EVENT_TYPE;
import static org.jbpm.ruleflow.core.Metadata.EVENT_TYPE_MESSAGE;
import static org.jbpm.ruleflow.core.Metadata.MAPPING_VARIABLE;
import static org.jbpm.ruleflow.core.Metadata.MESSAGE_TYPE;
import static org.jbpm.ruleflow.core.Metadata.TRIGGER_MAPPING;
import static org.jbpm.ruleflow.core.Metadata.TRIGGER_MAPPING_INPUT;
import static org.jbpm.ruleflow.core.Metadata.TRIGGER_REF;
import static org.jbpm.ruleflow.core.Metadata.TRIGGER_TYPE;

public class StartEventHandler extends AbstractNodeHandler {

    @Override
    protected Node createNode(Attributes attrs) {
        return new StartNode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class generateNodeFor() {
        return StartNode.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Node handleNode(final Node node, final Element element, final String uri,
            final String localName, final Parser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        StartNode startNode = (StartNode) node;
        // TODO: StartEventHandler.handleNode(): the parser doesn't discriminate between the schema default and the actual set value
        // However, while the schema says the "isInterrupting" attr should default to true
        // The spec says that Escalation start events should default to not interrupting..

        startNode.setInterrupting(Boolean.parseBoolean(element.getAttribute("isInterrupting")));
        startNode.setIoSpecification(readCatchSpecification(parser, element));
        findTargetMappingVar(startNode.getIoSpecification().getDataOutputAssociation()).ifPresent(data -> {
            startNode.getMetaData().put(TRIGGER_MAPPING, data.getLabel());
            startNode.getMetaData().put(MAPPING_VARIABLE, data.getLabel());
        });
        findSourceMappingVar(startNode.getIoSpecification().getDataOutputAssociation()).ifPresent(data -> startNode.getMetaData().put(TRIGGER_MAPPING_INPUT, data.getLabel()));

        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("conditionalEventDefinition".equals(nodeName)) {
                String constraint = null;
                org.w3c.dom.Node subNode = xmlNode.getFirstChild();
                while (subNode != null) {
                    String subnodeName = subNode.getNodeName();
                    if ("condition".equals(subnodeName)) {
                        constraint = xmlNode.getTextContent();
                        break;
                    }
                    subNode = subNode.getNextSibling();
                }
                ConstraintTrigger trigger = new ConstraintTrigger();
                trigger.setConstraint(constraint);
                startNode.addTrigger(trigger);
                break;
            } else if ("signalEventDefinition".equals(nodeName)) {
                String type = ((Element) xmlNode).getAttribute("signalRef");

                type = checkSignalAndConvertToRealSignalNam(parser, type);

                if (type != null && type.trim().length() > 0) {
                    addTriggerWithInMappings(startNode, type);
                }
                startNode.setMetaData(MESSAGE_TYPE, type);
                startNode.setMetaData(TRIGGER_TYPE, TriggerMetaData.TriggerType.Signal.name());
                Signal signal = findSignalByName(parser, type);
                if (signal != null) {
                    String eventType = signal.getStructureRef();
                    startNode.setMetaData(TRIGGER_REF, eventType);
                } else {
                    startNode.setMetaData(TRIGGER_REF, type);
                }
            } else if ("messageEventDefinition".equals(nodeName)) {
                String messageRef = ((Element) xmlNode).getAttribute("messageRef");
                Map<String, Message> messages = (Map<String, Message>) ((ProcessBuildData) parser.getData()).getMetaData("Messages");
                if (messages == null) {
                    throw new ProcessParsingValidationException("No messages found");
                }
                Message message = messages.get(messageRef);
                if (message == null) {
                    throw new ProcessParsingValidationException("Could not find message " + messageRef);
                }
                startNode.setMetaData(EVENT_TYPE, EVENT_TYPE_MESSAGE);
                startNode.setMetaData(MESSAGE_TYPE, message.getType());
                startNode.setMetaData(TRIGGER_TYPE, TriggerMetaData.TriggerType.ConsumeMessage.name());
                startNode.setMetaData(TRIGGER_REF, message.getName());

                addTriggerWithInMappings(startNode, "Message-" + message.getName(), message.getId(), ((RuleFlowProcess) parser.getMetaData().get("CurrentProcessDefinition")).getCorrelationManager());
            } else if ("timerEventDefinition".equals(nodeName)) {
                handleTimerNode(startNode, element, uri, localName, parser);
                // following event definitions are only for event sub process and will be validated to not be included in top process definitions
            } else if ("errorEventDefinition".equals(nodeName)) {
                // BPMN2 spec (p.245-246, (2011-01-03)) implies that
                //   - a <startEvent> in an Event Sub-Process
                //    - *without* the 'isInterupting' attribute always interrupts (containing process)
                startNode.setInterrupting(true);

                String errorRef = ((Element) xmlNode).getAttribute("errorRef");
                if (errorRef != null && errorRef.trim().length() > 0) {
                    List<Error> errors = (List<Error>) ((ProcessBuildData) parser.getData()).getMetaData("Errors");
                    if (errors == null) {
                        throw new ProcessParsingValidationException("No errors found");
                    }
                    Error error = null;
                    for (Error listError : errors) {
                        if (errorRef.equals(listError.getId())) {
                            error = listError;
                        }
                    }
                    if (error == null) {
                        throw new ProcessParsingValidationException("Could not find error " + errorRef);
                    }
                    startNode.setMetaData("FaultCode", error.getErrorCode());
                    startNode.setMetaData(MESSAGE_TYPE, error.getErrorCode());
                    startNode.setMetaData(TRIGGER_REF, error.getErrorCode());
                    startNode.setMetaData(TRIGGER_TYPE, TriggerMetaData.TriggerType.Signal.name());

                    addTriggerWithInMappings(startNode, "Error-" + error.getErrorCode());
                }
            } else if ("escalationEventDefinition".equals(nodeName)) {
                String escalationRef = ((Element) xmlNode).getAttribute("escalationRef");
                if (escalationRef != null && escalationRef.trim().length() > 0) {
                    Map<String, Escalation> escalations = (Map<String, Escalation>) ((ProcessBuildData) parser.getData()).getMetaData(ProcessHandler.ESCALATIONS);
                    if (escalations == null) {
                        throw new ProcessParsingValidationException("No escalations found");
                    }
                    Escalation escalation = escalations.get(escalationRef);
                    if (escalation == null) {
                        throw new ProcessParsingValidationException("Could not find escalation " + escalationRef);
                    }

                    addTriggerWithInMappings(startNode, "Escalation-" + escalation.getEscalationCode());
                }
            } else if ("compensateEventDefinition".equals(nodeName)) {
                handleCompensationNode(startNode, xmlNode);
            }
            xmlNode = xmlNode.getNextSibling();
        }

        if (startNode.getName() == null) {
            startNode.setName("Start");
        }
        return startNode;
    }

    private void addTriggerWithInMappings(StartNode startNode, String triggerEventType) {
        this.addTriggerWithInMappings(startNode, triggerEventType, null, null);
    }

    private void addTriggerWithInMappings(StartNode startNode, String triggerEventType, String messageRef, CorrelationManager manager) {
        EventTrigger trigger = new EventTrigger();
        EventTypeFilter eventFilter = new EventTypeFilter();
        eventFilter.setCorrelationManager(manager);
        eventFilter.setType(triggerEventType);
        eventFilter.setMessageRef(messageRef);
        trigger.addEventFilter(eventFilter);
        String mapping = (String) startNode.getMetaData(TRIGGER_MAPPING);
        if (mapping != null) {
            startNode.getIoSpecification().getDataOutputAssociation().forEach(da -> trigger.addInAssociation(da));
        }

        startNode.addTrigger(trigger);
    }

    @Override
    public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
        StartNode startNode = (StartNode) node;
        writeNode("startEvent", startNode, xmlDump, metaDataType);
        xmlDump.append(" isInterrupting=\"");
        if (startNode.isInterrupting()) {
            xmlDump.append("true");
        } else {
            xmlDump.append("false");
        }
        xmlDump.append("\">" + EOL);
        writeExtensionElements(startNode, xmlDump);

        List<Trigger> triggers = startNode.getTriggers();
        if (triggers != null) {
            if (triggers.size() > 1) {
                throw new ProcessParsingValidationException("Multiple start triggers not supported");
            }

            Trigger trigger = triggers.get(0);
            if (trigger instanceof ConstraintTrigger) {
                ConstraintTrigger constraintTrigger = (ConstraintTrigger) trigger;
                if (constraintTrigger.getHeader() == null) {
                    xmlDump.append("      <conditionalEventDefinition>" + EOL);
                    xmlDump.append(
                            "        <condition xsi:type=\"tFormalExpression\" language=\"" + XmlBPMNProcessDumper.RULE_LANGUAGE + "\">" + constraintTrigger.getConstraint() + "</condition>" + EOL);
                    xmlDump.append("      </conditionalEventDefinition>" + EOL);
                }
            } else if (trigger instanceof EventTrigger) {
                EventTrigger eventTrigger = (EventTrigger) trigger;
                String mapping = null;
                String nameMapping = "event";
                if (!trigger.getInMappings().isEmpty()) {
                    mapping = eventTrigger.getInMappings().keySet().iterator().next();
                    nameMapping = eventTrigger.getInMappings().values().iterator().next();
                } else {
                    mapping = (String) startNode.getMetaData(TRIGGER_MAPPING);
                }

                if (mapping != null) {
                    xmlDump.append(
                            "      <dataOutput id=\"_" + startNode.getId() + "_Output\" name=\"" + nameMapping + "\" />" + EOL +
                                    "      <dataOutputAssociation>" + EOL +
                                    "        <sourceRef>_" + startNode.getId() + "_Output</sourceRef>" + EOL +
                                    "        <targetRef>" + mapping + "</targetRef>" + EOL +
                                    "      </dataOutputAssociation>" + EOL);
                }

                String type = ((EventTypeFilter) eventTrigger.getEventFilters().get(0)).getType();
                if (type.startsWith("Message-")) {
                    type = type.substring(8);
                    xmlDump.append("      <messageEventDefinition messageRef=\"" + type + "\"/>" + EOL);
                } else if (type.startsWith("Error-")) {
                    type = type.substring(6);
                    String errorId = getErrorIdForErrorCode(type, startNode);
                    xmlDump.append("      <errorEventDefinition errorRef=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(errorId) + "\"/>" + EOL);
                } else if (type.startsWith("Escalation-")) {
                    type = type.substring(11);
                    xmlDump.append("      <escalationEventDefinition escalationRef=\"" + type + "\"/>" + EOL);
                } else if (type.equals("Compensation")) {
                    xmlDump.append("      <compensateEventDefinition/>" + EOL);
                } else {
                    xmlDump.append("      <signalEventDefinition signalRef=\"" + type + "\" />" + EOL);
                }
            } else {
                throw new ProcessParsingValidationException("Unsupported trigger type " + trigger);
            }

            if (startNode.getTimer() != null) {
                Timer timer = startNode.getTimer();
                xmlDump.append("      <timerEventDefinition>" + EOL);
                if (timer != null && (timer.getDelay() != null || timer.getDate() != null)) {
                    if (timer.getTimeType() == Timer.TIME_DATE) {
                        xmlDump.append("        <timeDate xsi:type=\"tFormalExpression\">" + XmlDumper.replaceIllegalChars(timer.getDate()) + "</timeDate>" + EOL);
                    } else if (timer.getTimeType() == Timer.TIME_DURATION) {
                        xmlDump.append("        <timeDuration xsi:type=\"tFormalExpression\">" + XmlDumper.replaceIllegalChars(timer.getDelay()) + "</timeDuration>" + EOL);
                    } else if (timer.getTimeType() == Timer.TIME_CYCLE) {

                        if (timer.getPeriod() != null) {
                            xmlDump.append("        <timeCycle xsi:type=\"tFormalExpression\">" + XmlDumper.replaceIllegalChars(timer.getDelay()) + "###"
                                    + XmlDumper.replaceIllegalChars(timer.getPeriod()) + "</timeCycle>" + EOL);
                        } else {
                            xmlDump.append("        <timeCycle xsi:type=\"tFormalExpression\">" + XmlDumper.replaceIllegalChars(timer.getDelay()) + "</timeCycle>" + EOL);
                        }
                    }
                }
                xmlDump.append("      </timerEventDefinition>" + EOL);
            }
        } else if (startNode.getTimer() != null) {
            Timer timer = startNode.getTimer();
            xmlDump.append("      <timerEventDefinition>" + EOL);
            if (timer != null && (timer.getDelay() != null || timer.getDate() != null)) {
                if (timer.getTimeType() == Timer.TIME_DATE) {
                    xmlDump.append("        <timeDate xsi:type=\"tFormalExpression\">" + XmlDumper.replaceIllegalChars(timer.getDate()) + "</timeDate>" + EOL);
                } else if (timer.getTimeType() == Timer.TIME_DURATION) {
                    xmlDump.append("        <timeDuration xsi:type=\"tFormalExpression\">" + XmlDumper.replaceIllegalChars(timer.getDelay()) + "</timeDuration>" + EOL);
                } else if (timer.getTimeType() == Timer.TIME_CYCLE) {

                    if (timer.getPeriod() != null) {
                        xmlDump.append("        <timeCycle xsi:type=\"tFormalExpression\">" + XmlDumper.replaceIllegalChars(timer.getDelay()) + "###" + XmlDumper.replaceIllegalChars(timer.getPeriod())
                                + "</timeCycle>" + EOL);
                    } else {
                        xmlDump.append("        <timeCycle xsi:type=\"tFormalExpression\">" + XmlDumper.replaceIllegalChars(timer.getDelay()) + "</timeCycle>" + EOL);
                    }
                }
            }
            xmlDump.append("      </timerEventDefinition>" + EOL);
        }
        endNode("startEvent", xmlDump);
    }

    protected void handleTimerNode(final Node node, final Element element,
            final String uri, final String localName,
            final Parser parser) throws SAXException {
        super.handleNode(node, element, uri, localName, parser);
        StartNode startNode = (StartNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("timerEventDefinition".equals(nodeName)) {
                Timer timer = new Timer();
                org.w3c.dom.Node subNode = xmlNode.getFirstChild();
                while (subNode instanceof Element) {
                    String subNodeName = subNode.getNodeName();
                    if ("timeCycle".equals(subNodeName)) {
                        String delay = subNode.getTextContent();
                        int index = delay.indexOf("###");
                        if (index != -1) {
                            String period = delay.substring(index + 3);
                            delay = delay.substring(0, index);
                            timer.setPeriod(period);
                        } else {
                            timer.setPeriod(delay);
                        }
                        timer.setTimeType(Timer.TIME_CYCLE);
                        timer.setDelay(delay);
                        break;
                    } else if ("timeDuration".equals(subNodeName)) {
                        String delay = subNode.getTextContent();
                        timer.setTimeType(Timer.TIME_DURATION);
                        timer.setDelay(delay);
                        break;
                    } else if ("timeDate".equals(subNodeName)) {
                        String date = subNode.getTextContent();
                        timer.setTimeType(Timer.TIME_DATE);
                        timer.setDate(date);
                        break;
                    }
                    subNode = subNode.getNextSibling();
                }
                startNode.setTimer(timer);
                if (parser.getParent() instanceof EventSubProcessNode) {
                    // handle timer on start events like normal (non rule) timers for event sub process

                    EventTrigger trigger = new EventTrigger();
                    EventTypeFilter eventFilter = new EventTypeFilter();
                    eventFilter.setType("Timer-" + ((EventSubProcessNode) parser.getParent()).getId());
                    trigger.addEventFilter(eventFilter);
                    String mapping = (String) startNode.getMetaData(TRIGGER_MAPPING);
                    if (mapping != null) {
                        trigger.addInMapping("event", mapping);
                    }
                    startNode.addTrigger(trigger);
                    ((EventSubProcessNode) parser.getParent()).addTimer(timer, new DroolsConsequenceAction("java", ""));
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }

    protected void handleCompensationNode(final StartNode startNode, final org.w3c.dom.Node xmlNode) {
        if (startNode.isInterrupting()) {
            logger.warn("Compensation Event Sub-Processes [" + startNode.getMetaData("UniqueId") + "] may not be specified as interrupting:" +
                    " overriding attribute and setting to not-interrupting.");
        }
        startNode.setInterrupting(false);

        /**
         * From the BPMN2 spec, P.264:
         * "For a Start Event:
         * This Event "catches" the compensation for an Event Sub-Process. No further information is required.
         * The Event Sub-Process will provide the id necessary to match the Compensation Event with the Event
         * that threw the compensation"
         *
         * In other words, the id of the Sub-Process containing this Event Sub-Process is what should be used
         * as the activityRef value in any Intermediate (throw) or End compensation event that targets
         * this particular Event Sub-Process.
         *
         * This is similar to the logic used for a Compensation Boundary Event: it's signaled using
         * the id of the activity to which the CBE is attached to.
         */
        String activityRef = ((Element) xmlNode).getAttribute("activityRef");
        if (activityRef != null && activityRef.length() > 0) {
            logger.warn("activityRef value [" + activityRef + "] on Start Event '" + startNode.getMetaData("UniqueId")
                    + "' ignored per the BPMN2 specification.");
        }

        // so that this node will get processed in ProcessHandler.postProcessNodes(...)
        EventTrigger startTrigger = new EventTrigger();
        EventFilter eventFilter = new NonAcceptingEventTypeFilter();
        ((NonAcceptingEventTypeFilter) eventFilter).setType("Compensation");
        startTrigger.addEventFilter(eventFilter);
        List<Trigger> startTriggers = new ArrayList<>();
        startTriggers.add(startTrigger);
        startNode.setTriggers(startTriggers);
        String mapping = (String) startNode.getMetaData(TRIGGER_MAPPING);
        if (mapping != null) {
            startNode.getIoSpecification().getDataOutputAssociation().forEach(da -> startTrigger.addInAssociation(da));
        }
    }
}
