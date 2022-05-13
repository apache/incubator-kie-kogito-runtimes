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

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jbpm.bpmn2.core.ItemDefinition;
import org.jbpm.bpmn2.core.SequenceFlow;
import org.jbpm.compiler.xml.Handler;
import org.jbpm.compiler.xml.Parser;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.compiler.xml.core.BaseAbstractHandler;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.datatype.DataTypeResolver;
import org.jbpm.workflow.core.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DataObjectHandler extends BaseAbstractHandler implements Handler {

    public DataObjectHandler() {
        initValidParents();
        initValidPeers();
        this.allowNesting = false;
    }

    protected void initValidParents() {
        this.validParents = new HashSet<Class<?>>();
        this.validParents.add(ContextContainer.class);
    }

    protected void initValidPeers() {
        this.validPeers = new HashSet<Class<?>>();
        this.validPeers.add(null);
        this.validPeers.add(Variable.class);
        this.validPeers.add(Node.class);
        this.validPeers.add(SequenceFlow.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object start(final String uri, final String localName,
            final Attributes attrs, final Parser parser)
            throws SAXException {
        parser.startElementBuilder(localName, attrs);

        final String id = attrs.getValue("id");
        final String itemSubjectRef = attrs.getValue("itemSubjectRef");

        Object parent = parser.getParent();
        if (parent instanceof ContextContainer) {
            ContextContainer contextContainer = (ContextContainer) parent;
            VariableScope variableScope = (VariableScope) contextContainer.getDefaultContext(VariableScope.VARIABLE_SCOPE);
            List variables = variableScope.getVariables();
            Variable variable = new Variable();
            variable.setMetaData("DataObject", "true");
            variable.setId(id);
            variable.setName(id);
            variable.setMetaData(id, variable.getName());
            // retrieve type from item definition
            DataType dataType = DataTypeResolver.defaultDataType;
            Map<String, ItemDefinition> itemDefinitions = (Map<String, ItemDefinition>) ((ProcessBuildData) parser.getData()).getMetaData("ItemDefinitions");
            if (itemDefinitions != null) {
                ItemDefinition itemDefinition = itemDefinitions.get(itemSubjectRef);
                if (itemDefinition != null) {
                    dataType = DataTypeResolver.fromType(itemDefinition.getStructureRef(), parser.getClassLoader());
                }
            }
            variable.setType(dataType);
            variables.add(variable);
            return variable;
        }

        return new Variable();
    }

    @Override
    public Object end(final String uri, final String localName,
            final Parser parser) throws SAXException {
        parser.endElementBuilder();
        return parser.getCurrent();
    }

    @Override
    public Class<?> generateNodeFor() {
        return Variable.class;
    }

}
