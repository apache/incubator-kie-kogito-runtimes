/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.marshalling.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.process.instance.KogitoWorkItem;
import org.drools.core.process.instance.impl.KogitoWorkItemImpl;
import org.jbpm.marshalling.impl.JBPMMessages.Variable;
import org.jbpm.marshalling.impl.JBPMMessages.VariableContainer;
import org.kie.kogito.internal.marshalling.ObjectMarshallingStrategy;
import org.kie.kogito.internal.runtime.process.WorkItem;

public class ProtobufProcessMarshaller {

	private static boolean persistWorkItemVars = Boolean.parseBoolean(System.getProperty("org.jbpm.wi.variable.persist", "true"));
	// mainly for testability as the setting is global
	public static void setWorkItemVarsPersistence(boolean turnOn) {
		persistWorkItemVars = turnOn;
	}

    public static JBPMMessages.WorkItem writeWorkItem(MarshallerWriteContext context,
                                                      WorkItem workItem,
                                                      boolean includeVariables) throws IOException {
        JBPMMessages.WorkItem.Builder _workItem = JBPMMessages.WorkItem.newBuilder()
                .setId( workItem.getId() )
                .setProcessInstancesId( workItem.getProcessInstanceId() )
                .setName( workItem.getName() )
                .setState( workItem.getState() );

        if (workItem instanceof KogitoWorkItem ) {
        	if ((( KogitoWorkItem )workItem).getDeploymentId() != null){
        	_workItem.setDeploymentId((( KogitoWorkItem )workItem).getDeploymentId());
        	}
        	_workItem.setNodeId((( KogitoWorkItem )workItem).getNodeId())
        	.setNodeInstanceId((( KogitoWorkItem )workItem).getNodeInstanceStringId());
        }

        if ( includeVariables ) {
            Map<String, Object> parameters = workItem.getParameters();
            for ( Map.Entry<String, Object> entry : parameters.entrySet() ) {
                _workItem.addVariable( marshallVariable( context, entry.getKey(), entry.getValue() ) );
            }
        }
        return _workItem.build();
    }

    public static WorkItem readWorkItem(MarshallerReaderContext context,
                                        JBPMMessages.WorkItem _workItem ) throws IOException {
        return readWorkItem( context,
                             _workItem,
                             true );
    }

    public static WorkItem readWorkItem(MarshallerReaderContext context,
                                        JBPMMessages.WorkItem _workItem,
                                        boolean includeVariables) throws IOException {
        KogitoWorkItemImpl workItem = new KogitoWorkItemImpl();
        workItem.setId( _workItem.getId() );
        workItem.setProcessInstanceId( _workItem.getProcessInstancesId() );
        workItem.setName( _workItem.getName() );
        workItem.setState( _workItem.getState() );
        workItem.setDeploymentId(_workItem.getDeploymentId());
        workItem.setNodeId(_workItem.getNodeId());
        workItem.setNodeInstanceId(_workItem.getNodeInstanceId());

        if ( includeVariables ) {
            for ( JBPMMessages.Variable _variable : _workItem.getVariableList() ) {
                try {
                    Object value = unmarshallVariableValue( context, _variable );
                    workItem.setParameter( _variable.getName(),
                                           value );
                } catch ( ClassNotFoundException e ) {
                    throw new IllegalArgumentException( "Could not reload parameter " + _variable.getName() + " for work item " + _workItem );
                }
            }
        }

        return workItem;
    }
    
    private static String getType (ObjectMarshallingStrategy strategy, Class<?> clazz) {
        return strategy instanceof org.kie.kogito.internal.marshalling.ObjectMarshallingStrategy
                ? strategy.getType(clazz)
                : clazz.getCanonicalName();
    }

    public static Variable marshallVariable(MarshallerWriteContext context,
                                            String name,
                                            Object value) throws IOException {
        JBPMMessages.Variable.Builder builder = JBPMMessages.Variable.newBuilder().setName( name );
        if(value != null){
            ObjectMarshallingStrategy strategy = context.getObjectMarshallingStrategyStore().getStrategyObject( value );
            Integer index = context.getStrategyIndex( strategy );
            builder.setStrategyIndex( index )
                   .setDataType(getType(strategy, value.getClass()))
                   .setValue( ByteString.copyFrom( strategy.marshal( context.getStrategyContext().get( strategy ),
                                                                     (ObjectOutputStream) context,
                                                                     value ) ) );
        }
        return builder.build();
    }

    public static Variable marshallVariablesMap(MarshallerWriteContext context, Map<String, Object> variables) throws IOException{
        Map<String, Variable> marshalledVariables = new HashMap<String, Variable>();
        for(String key : variables.keySet()){
            JBPMMessages.Variable.Builder builder = JBPMMessages.Variable.newBuilder().setName( key );
            Object variable = variables.get(key);
            if(variable != null){
                ObjectMarshallingStrategy strategy = context.getObjectMarshallingStrategyStore().getStrategyObject( variable );
                Integer index = context.getStrategyIndex( strategy );
                builder.setStrategyIndex( index )
                       .setDataType(getType(strategy, variable.getClass()))
                       .setValue( ByteString.copyFrom( strategy.marshal( context.getStrategyContext().get( strategy ),
                                                                     ( ObjectOutputStream ) context,
                                                                     variable ) ) );

            }



            marshalledVariables.put(key, builder.build());
        }

        return marshallVariable(context, "variablesMap" ,marshalledVariables);
    }

    public static VariableContainer marshallVariablesContainer(MarshallerWriteContext context, Map<String, Object> variables) throws IOException{
    	JBPMMessages.VariableContainer.Builder vcbuilder = JBPMMessages.VariableContainer.newBuilder();
        for(String key : variables.keySet()){
            JBPMMessages.Variable.Builder builder = JBPMMessages.Variable.newBuilder().setName( key );
            if(variables.get(key) != null){
                ObjectMarshallingStrategy strategy = context.getObjectMarshallingStrategyStore().getStrategyObject( variables.get(key) );
                Integer index = context.getStrategyIndex( strategy );
                builder.setStrategyIndex( index )
                   .setValue( ByteString.copyFrom( strategy.marshal( context.getStrategyContext().get( strategy ),
                                                                     (ObjectOutputStream) context,
                                                                     variables.get(key) ) ) );

            }



            vcbuilder.addVariable(builder.build());
        }

        return vcbuilder.build();
    }

    public static Object unmarshallVariableValue(MarshallerReaderContext context,
                                                  JBPMMessages.Variable _variable) throws IOException,
                                                                                  ClassNotFoundException {
        if(_variable.getValue() == null || _variable.getValue().isEmpty()){
            return null;
        }
        ObjectMarshallingStrategy strategy = context.getUsedStrategies().get( _variable.getStrategyIndex() );
        return strategy instanceof org.kie.kogito.internal.marshalling.ObjectMarshallingStrategy
                ? ((org.kie.kogito.internal.marshalling.ObjectMarshallingStrategy) strategy).unmarshal(
                        _variable.getDataType(),
                        context.getStrategyContexts().get(strategy),
                        (ObjectInputStream) context,
                        _variable.getValue().toByteArray(),
                        (context.getKnowledgeBase() == null) ? null : context.getKnowledgeBase().getRootClassLoader())
                : strategy.unmarshal(
                        context.getStrategyContexts().get(strategy),
                        (ObjectInputStream) context,
                        _variable.getValue().toByteArray(),
                        (context.getKnowledgeBase() == null) ? null : context.getKnowledgeBase().getRootClassLoader());
    }

	public static Map<String, Object> unmarshallVariableContainerValue(MarshallerReaderContext context, JBPMMessages.VariableContainer _variableContiner)
			throws IOException, ClassNotFoundException {
		Map<String, Object> variables = new HashMap<>();
		if (_variableContiner.getVariableCount() == 0) {
			return variables;
		}

		for (Variable _variable : _variableContiner.getVariableList()) {

			Object value = ProtobufProcessMarshaller.unmarshallVariableValue(context, _variable);

			variables.put(_variable.getName(), value);
		}
		return variables;
	}

    public void init(MarshallerReaderContext context) {
        ExtensionRegistry registry = (ExtensionRegistry) context.getParameterObject();
        registry.add( JBPMMessages.processInstance );
        registry.add( JBPMMessages.processTimer );
        registry.add( JBPMMessages.procTimer );
        registry.add( JBPMMessages.workItem );
        registry.add( JBPMMessages.timerId );
    }

  
}
