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
package org.jbpm.serverless.workflow.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.jbpm.serverless.workflow.api.Workflow;
import org.jbpm.serverless.workflow.api.actions.Action;
import org.jbpm.serverless.workflow.api.branches.Branch;
import org.jbpm.serverless.workflow.api.events.EventDefinition;
import org.jbpm.serverless.workflow.api.events.OnEvents;
import org.jbpm.serverless.workflow.api.functions.FunctionDefinition;
import org.jbpm.serverless.workflow.api.interfaces.WorkflowValidator;
import org.jbpm.serverless.workflow.api.states.*;
import org.jbpm.serverless.workflow.api.switchconditions.EventCondition;
import org.jbpm.serverless.workflow.api.validation.ValidationError;
import org.jbpm.serverless.workflow.api.validation.WorkflowSchemaLoader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkflowValidatorImpl implements WorkflowValidator {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowValidatorImpl.class);
    private boolean schemaValidationEnabled = true;
    private List<ValidationError> validationErrors = new ArrayList();
    private Schema workflowSchema = WorkflowSchemaLoader.getWorkflowSchema();
    private String source;
    private Workflow workflow;

    @Override
    public WorkflowValidator setWorkflow(Workflow workflow) {
        this.workflow = workflow;
        return this;
    }

    @Override
    public WorkflowValidator setSource(String source) {
        this.source = source;
        return this;
    }

    @Override
    public List<ValidationError> validate() {
        validationErrors.clear();
        if (workflow == null) {
            try {
                if (schemaValidationEnabled && source != null) {
                    try {
                        if (!source.trim().startsWith("{")) {
                            // convert yaml to json to validate
                            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                            Object obj = yamlReader.readValue(source, Object.class);

                            ObjectMapper jsonWriter = new ObjectMapper();

                            workflowSchema.validate(new JSONObject(jsonWriter.writeValueAsString(obj)));
                        } else {
                            workflowSchema.validate(new JSONObject(source));
                        }
                    } catch (ValidationException e) {
                        // main error
                        addValidationError(e.getMessage(),
                                ValidationError.SCHEMA_VALIDATION);
                        // suberrors
                        e.getCausingExceptions().stream()
                                .map(ValidationException::getMessage)
                                .forEach(m -> addValidationError(m,
                                        ValidationError.SCHEMA_VALIDATION));
                    }
                }
            } catch (Exception e) {
                logger.error("Schema validation exception: " + e.getMessage());
            }
        }

        // if there are schema validation errors
        // there is no point of doing the workflow validation
        if (validationErrors.size() > 0) {
            return validationErrors;
        } else {
            if (workflow == null) {
                workflow = Workflow.fromSource(source);
            }

            List<FunctionDefinition> functions = workflow.getFunctions();
            List<EventDefinition> events = workflow.getEvents();

            if (workflow.getId() == null || workflow.getId().trim().isEmpty()) {
                addValidationError("Workflow id should not be empty",
                        ValidationError.WORKFLOW_VALIDATION);
            }

            if (workflow.getName() == null || workflow.getName().trim().isEmpty()) {
                addValidationError("Workflow name should not be empty",
                        ValidationError.WORKFLOW_VALIDATION);
            }

            if (workflow.getVersion() == null || workflow.getVersion().trim().isEmpty()) {
                addValidationError("Workflow version should not be empty",
                        ValidationError.WORKFLOW_VALIDATION);
            }

            if (workflow.getStates() == null || workflow.getStates().isEmpty()) {
                addValidationError("No states found",
                        ValidationError.WORKFLOW_VALIDATION);
            }

            Validation validation = new Validation();
            if (workflow.getStates() != null && !workflow.getStates().isEmpty()) {
                workflow.getStates().forEach(s -> {
                    if (s.getName() != null && s.getName().trim().isEmpty()) {
                        addValidationError("State name should not be empty",
                                ValidationError.WORKFLOW_VALIDATION);
                    } else {
                        validation.addState(s.getName());
                    }

                    if (s.getStart() != null) {
                        validation.addStartState();
                    }

                    if (s.getEnd() != null) {
                        validation.addEndState();
                    }

                    if (s instanceof OperationState) {
                        OperationState operationState = (OperationState) s;
                        if (operationState.getActions() == null || operationState.getActions().size() < 1) {
                            addValidationError("Operation State has no actions defined",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }

                        List<Action> actions = operationState.getActions();
                        for (Action action : actions) {
                            if (action.getFunctionRef() != null) {
                                if (action.getFunctionRef().getRefName().isEmpty()) {
                                    addValidationError("Operation State action functionRef should not be null or empty",
                                            ValidationError.WORKFLOW_VALIDATION);
                                }

                                if (!haveFunctionDefinition(action.getFunctionRef().getRefName(), functions)) {
                                    addValidationError("Operation State action functionRef does not reference an existing workflow function definition",
                                            ValidationError.WORKFLOW_VALIDATION);
                                }
                            }

                            if (action.getEventRef() != null) {
                                if (action.getEventRef().getTriggerEventRef().isEmpty()) {
                                    addValidationError("Operation State action trigger eventRef does not reference an existing workflow event definition",
                                            ValidationError.WORKFLOW_VALIDATION);
                                }

                                if (action.getEventRef().getResultEventRef().isEmpty()) {
                                    addValidationError("Operation State action results eventRef does not reference an existing workflow event definition",
                                            ValidationError.WORKFLOW_VALIDATION);
                                }

                                if (!haveEventsDefinition(action.getEventRef().getTriggerEventRef(), events)) {
                                    addValidationError("Operation State action trigger event def does not reference an existing workflow event definition",
                                            ValidationError.WORKFLOW_VALIDATION);
                                }

                                if (!haveEventsDefinition(action.getEventRef().getResultEventRef(), events)) {
                                    addValidationError("Operation State action results event def does not reference an existing workflow event definition",
                                            ValidationError.WORKFLOW_VALIDATION);
                                }
                            }
                        }
                    }

                    if (s instanceof EventState) {
                        EventState eventState = (EventState) s;
                        if (eventState.getOnEvents() == null || eventState.getOnEvents().size() < 1) {
                            addValidationError("Event State has no eventActions defined",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }
                        List<OnEvents> eventsActionsList = eventState.getOnEvents();
                        for (OnEvents onEvents : eventsActionsList) {
                            if (onEvents.getActions() == null || onEvents.getActions().size() < 1) {
                                addValidationError("Event State eventsActions has no actions",
                                        ValidationError.WORKFLOW_VALIDATION);
                            }

                            List<String> eventRefs = onEvents.getEventRefs();
                            if (eventRefs == null || eventRefs.size() < 1) {
                                addValidationError("Event State eventsActions has no event refs",
                                        ValidationError.WORKFLOW_VALIDATION);
                            } else {
                                for (String eventRef : eventRefs) {
                                    if (!haveEventsDefinition(eventRef, events)) {
                                        addValidationError("Event State eventsActions eventRef does not match a declared workflow event definition",
                                                ValidationError.WORKFLOW_VALIDATION);
                                    }
                                }
                            }
                        }
                    }

                    if (s instanceof SwitchState) {
                        SwitchState switchState = (SwitchState) s;
                        if ((switchState.getDataConditions() == null || switchState.getDataConditions().size() < 1)
                                && (switchState.getEventConditions() == null || switchState.getEventConditions().size() < 1)) {
                            addValidationError("Switch state should define either data or event conditions",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }

                        if (switchState.getDefault() == null) {
                            addValidationError("Switch state should define a default transition",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }

                        if (switchState.getEventConditions() != null && switchState.getEventConditions().size() > 0) {
                            List<EventCondition> eventConditions = switchState.getEventConditions();
                            for (EventCondition ec : eventConditions) {
                                if (!haveEventsDefinition(ec.getEventRef(), events)) {
                                    addValidationError("Switch state event condition eventRef does not reference a defined workflow event",
                                            ValidationError.WORKFLOW_VALIDATION);
                                }
                            }
                        }
                    }

                    if (s instanceof DelayState) {
                        DelayState delayState = (DelayState) s;
                        if (delayState.getTimeDelay() == null || delayState.getTimeDelay().length() < 1) {
                            addValidationError("Delay state should have a non-empty time delay",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }
                    }

                    if (s instanceof ParallelState) {
                        ParallelState parallelState = (ParallelState) s;
                        if (parallelState.getBranches() == null || parallelState.getBranches().size() < 1) {
                            addValidationError("Parallel state should have branches",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }

                        List<Branch> branches = parallelState.getBranches();
                        for (Branch branch : branches) {
                            if ((branch.getActions() == null || branch.getActions().size() < 1)
                                    && (branch.getWorkflowId() == null || branch.getWorkflowId().length() < 1)) {
                                addValidationError("Parallel state should define either actions or workflow id",
                                        ValidationError.WORKFLOW_VALIDATION);
                            }
                        }
                    }

                    if (s instanceof SubflowState) {
                        SubflowState subflowState = (SubflowState) s;
                        if (subflowState.getWorkflowId() == null || subflowState.getWorkflowId().isEmpty()) {
                            addValidationError("SubflowState should have a valid workflow id",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }
                    }

                    if (s instanceof InjectState) {
                        InjectState injectState = (InjectState) s;
                        if (injectState.getData() == null) {
                            addValidationError("InjectState should have non-null data",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }
                    }

                    if (s instanceof ForEachState) {
                        ForEachState forEachState = (ForEachState) s;
                        if (forEachState.getInputCollection() == null || forEachState.getInputCollection().isEmpty()) {
                            addValidationError("ForEach state should have a valid inputCollection",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }

                        if (forEachState.getIterationParam() == null || forEachState.getIterationParam().isEmpty()) {
                            addValidationError("ForEach state should have a valid iteration parameter",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }

                        if ((forEachState.getActions() == null || forEachState.getActions().size() < 1)
                                && (forEachState.getWorkflowId() == null || forEachState.getWorkflowId().length() < 1)) {
                            addValidationError("ForEach state should define either actions or workflow id",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }
                    }

                    if (s instanceof CallbackState) {
                        CallbackState callbackState = (CallbackState) s;

                        if (!haveEventsDefinition(callbackState.getEventRef(), events)) {
                            addValidationError("CallbackState event ref does not reference a defined workflow event definition",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }

                        if (haveFunctionDefinition(callbackState.getAction().getFunctionRef().getRefName(), functions)) {
                            addValidationError("CallbackState action function ref does not reference a defined workflow function definition",
                                    ValidationError.WORKFLOW_VALIDATION);
                        }
                    }

                });

                if (validation.startStates == 0) {
                    addValidationError("No start state found.",
                            ValidationError.WORKFLOW_VALIDATION);
                }

                if (validation.startStates > 1) {
                    addValidationError("Multiple start states found.",
                            ValidationError.WORKFLOW_VALIDATION);
                }

                if (validation.endStates == 0) {
                    addValidationError("No end state found.",
                            ValidationError.WORKFLOW_VALIDATION);
                }
            }


            return validationErrors;
        }
    }

    @Override
    public boolean isValid() {
        return validate().size() < 1;
    }

    @Override
    public WorkflowValidator setSchemaValidationEnabled(boolean schemaValidationEnabled) {
        this.schemaValidationEnabled = schemaValidationEnabled;
        return this;
    }

    @Override
    public WorkflowValidator reset() {
        workflow = null;
        validationErrors.clear();
        schemaValidationEnabled = true;
        return this;
    }

    private boolean haveFunctionDefinition(String functionName, List<FunctionDefinition> functions) {
        FunctionDefinition fun = functions.stream().filter(f -> f.getName().equals(functionName))
                .findFirst()
                .orElse(null);

        return fun == null ? false : true;
    }

    private boolean haveEventsDefinition(String eventName, List<EventDefinition> events) {
        EventDefinition eve = events.stream().filter(e -> e.getName().equals(eventName))
                .findFirst()
                .orElse(null);

        return eve == null ? false : true;
    }

    private void addValidationError(String message,
                                    String type) {
        ValidationError mainError = new ValidationError();
        mainError.setMessage(message);
        mainError.setType(type);
        validationErrors.add(mainError);
    }

    private class Validation {

        final Set<String> events = new HashSet<>();
        final Set<String> functions = new HashSet();
        final Set<String> states = new HashSet<>();
        Integer startStates = 0;
        Integer endStates = 0;

        void addFunction(String name) {
            if (functions.contains(name)) {
                addValidationError("Function does not have an unique name: " + name,
                        ValidationError.WORKFLOW_VALIDATION);
            } else {
                functions.add(name);
            }
        }

        void addEvent(String name) {
            if (events.contains(name)) {
                addValidationError("Event does not have an unique name: " + name,
                        ValidationError.WORKFLOW_VALIDATION);
            } else {
                events.add(name);
            }
        }

        void addState(String name) {
            if (states.contains(name)) {
                addValidationError("State does not have an unique name: " + name,
                        ValidationError.WORKFLOW_VALIDATION);
            } else {
                states.add(name);
            }
        }

        void addStartState() {
            startStates++;
        }

        void addEndState() {
            endStates++;
        }
    }
}
