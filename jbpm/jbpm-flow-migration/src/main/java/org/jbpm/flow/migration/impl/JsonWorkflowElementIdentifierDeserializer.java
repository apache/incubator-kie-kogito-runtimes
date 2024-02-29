package org.jbpm.flow.migration.impl;

import java.io.IOException;

import org.jbpm.ruleflow.core.WorkflowElementIdentifierFactory;
import org.kie.api.definition.process.WorkflowElementIdentifier;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class JsonWorkflowElementIdentifierDeserializer extends JsonDeserializer<WorkflowElementIdentifier> {

    @Override
    public WorkflowElementIdentifier deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return WorkflowElementIdentifierFactory.fromExternalFormat(p.getValueAsString());
    }

}
