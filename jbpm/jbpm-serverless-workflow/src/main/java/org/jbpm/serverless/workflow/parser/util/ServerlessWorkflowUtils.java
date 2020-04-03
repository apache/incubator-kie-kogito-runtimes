/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.serverless.workflow.parser.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.drools.core.util.StringUtils;
import org.jbpm.serverless.workflow.api.Workflow;
import org.jbpm.serverless.workflow.api.events.EventDefinition;
import org.jbpm.serverless.workflow.api.interfaces.State;
import org.jbpm.serverless.workflow.api.mapper.BaseObjectMapper;
import org.jbpm.serverless.workflow.api.mapper.JsonObjectMapper;
import org.jbpm.serverless.workflow.api.mapper.YamlObjectMapper;
import org.jbpm.serverless.workflow.api.states.DefaultState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.regex.Matcher;

public class ServerlessWorkflowUtils {

    public static final String DEFAULT_WORKFLOW_FORMAT = "json";
    public static final String ALTERNATE_WORKFLOW_FORMAT = "yml";
    public static final String DEFAULT_START_STATE_NAME = "StartState";
    public static final String DEFAULT_END_STATE_NAME = "EndState";
    public static final String DEFAULT_JSONPATH_CONFIG = "com.jayway.jsonpath.Configuration jsonPathConfig = com.jayway.jsonpath.Configuration.builder()" +
            ".mappingProvider(new com.jayway.jsonpath.spi.mapper.JacksonMappingProvider())" +
            ".jsonProvider(new com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider()).build(); ";


    private static final Logger LOGGER = LoggerFactory.getLogger(ServerlessWorkflowUtils.class);

    private ServerlessWorkflowUtils() {}

    public static BaseObjectMapper getObjectMapper(String workflowFormat) {
        if(workflowFormat != null && workflowFormat.equalsIgnoreCase(DEFAULT_WORKFLOW_FORMAT)) {
            return new JsonObjectMapper();
        }

        if(workflowFormat != null && workflowFormat.equalsIgnoreCase(ALTERNATE_WORKFLOW_FORMAT)) {
            return new YamlObjectMapper();
        }

        LOGGER.error("unable to determine workflow format {}", workflowFormat);
        throw new IllegalArgumentException("invalid workflow format");
    }

    public static String readWorkflowFile(Reader reader) {
        return StringUtils.readFileAsString(reader);
    }

    public static String getWorkflowStartStateName(Workflow workflow) {
        return getWorkflowStartState(workflow).getName() != null ? getWorkflowStartState(workflow).getName() : DEFAULT_START_STATE_NAME;
    }

    public static String getWorkflowEndStateName(Workflow workflow) {
        return getWorkflowEndState(workflow).getName() != null ? getWorkflowEndState(workflow).getName() : DEFAULT_END_STATE_NAME;
    }

    public static State getWorkflowStartState(Workflow workflow) {
        return workflow.getStates().stream()
                .filter(ws -> ws.getStart() != null)
                .findFirst().get();
    }

    public static State getWorkflowEndState(Workflow workflow) {
        return workflow.getStates().stream()
                .filter(ws -> ws.getEnd() != null)
                .findFirst().get();
    }

    public static boolean includesSupportedStates(Workflow workflow) {
        for(State state : workflow.getStates()) {
            if(!state.getType().equals(DefaultState.Type.EVENT)
                    && !state.getType().equals(DefaultState.Type.OPERATION)
                    && !state.getType().equals(DefaultState.Type.DELAY)
                    && !state.getType().equals(DefaultState.Type.SUBFLOW)
                    && !state.getType().equals(DefaultState.Type.RELAY)) {
                return false;
            }
        }

        return true;
    }

    public static EventDefinition getWorkflowEventFor(Workflow workflow, String eventName) {
        return workflow.getEvents().stream()
                .filter(wt -> wt.getName().equals(eventName))
                .findFirst().get();
    }


    public static String sysOutFunctionScript(String script) {
        String retStr = DEFAULT_JSONPATH_CONFIG;
        retStr += "String toPrint = \"\";";
        retStr += getJsonPathScript(script);
        retStr += "System.out.println(toPrint);";

        return retStr;
    }

    public static String scriptFunctionScript(String script) {
        String retStr = DEFAULT_JSONPATH_CONFIG;
        retStr += getJsonPathScript(script);
        return retStr;
    }

    public static String getJsonPathScript(String script) {

        if(script.indexOf("$") >= 0) {

            String replacement = "toPrint += com.jayway.jsonpath.JsonPath.using( jsonPathConfig )" +
                    ".parse(((com.fasterxml.jackson.databind.JsonNode)kcontext.getVariable(\"workflowdata\")))" +
                    ".read(\"@@.$1\", com.fasterxml.jackson.databind.JsonNode.class).textValue() + \" \";";
            script =  script.replaceAll("\\$.([A-Za-z]+)", replacement);
            script = script.replaceAll("@@", Matcher.quoteReplacement("$"));
            return script;
        } else {
            return script;
        }
    }

    public static String getInjectScript(JsonNode toInjectNode) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String injectStr = objectMapper.writeValueAsString(toInjectNode);

            return "com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();\n" +
                    "        com.fasterxml.jackson.databind.JsonNode updateNode2 = objectMapper.readTree(\"" + injectStr.replaceAll("\"", "\\\\\"") + "\");\n" +
                    "        com.fasterxml.jackson.databind.JsonNode mainNode2 = (com.fasterxml.jackson.databind.JsonNode)kcontext.getVariable(\"workflowdata\");\n" +
                    "        java.util.Iterator<String> fieldNames2 = updateNode2.fieldNames();\n" +
                    "        while(fieldNames2.hasNext()) {\n" +
                    "            String updatedFieldName = fieldNames2.next();\n" +
                    "            com.fasterxml.jackson.databind.JsonNode updatedValue = updateNode2.get(updatedFieldName);\n" +
                    "            if(mainNode2.get(updatedFieldName) != null) {\n" +
                    "                ((com.fasterxml.jackson.databind.node.ObjectNode) mainNode2).replace(updatedFieldName, updatedValue);\n" +
                    "            } else {\n" +
                    "                ((com.fasterxml.jackson.databind.node.ObjectNode) mainNode2).put(updatedFieldName, updatedValue);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        kcontext.setVariable(\"workflowdata\", mainNode2);\n";

        } catch(JsonProcessingException e) {
            LOGGER.warn("unable to set inject script: {}", e.getMessage());
            return "";
        }
    }

}
