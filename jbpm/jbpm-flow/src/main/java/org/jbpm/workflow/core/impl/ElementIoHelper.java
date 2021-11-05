/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workflow.core.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.drools.core.spi.KogitoProcessContextImpl;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.datatype.impl.type.StringDataType;
import org.jbpm.process.core.datatype.impl.type.UndefinedDataType;
import org.jbpm.process.core.impl.DataTransformerRegistry;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.AssignmentAction;
import org.jbpm.process.instance.impl.AssignmentProducer;
import org.jbpm.process.instance.impl.util.TypeTransformer;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.Transformation;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.runtime.process.DataTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class allows to simplify input output processing as a cross cutting concern (actions or node themselves)
 *
 */
public class ElementIoHelper {
    protected static final Logger logger = LoggerFactory.getLogger(ElementIoHelper.class);

    private NodeInstanceImpl nodeInstance;

    public ElementIoHelper(NodeInstanceImpl nodeInstance) {
        this.nodeInstance = nodeInstance;
    }

    private TypeTransformer typeTransformer = new TypeTransformer();

    public void processInputs(Collection<DataAssociation> dataAssociation, Function<String, Object> sourceResolver, Function<String, Object> targetResolver, AssignmentProducer producer) {
        BiFunction<Map<String, Object>, DataAssociation, Map<String, Object>> converter = (dataSet, mapping) -> {
            HashMap<String, Object> transformationDataSet = new HashMap<>();
            transformationDataSet.put(mapping.getTarget().getLabel(), dataSet.get(mapping.getSources().get(0).getLabel()));
            return transformationDataSet;
        };
        this.processDataAssociations(converter, dataAssociation, sourceResolver, targetResolver, producer);
    }

    public void processOutputs(Collection<DataAssociation> dataAssociation, Function<String, Object> sourceResolver, Function<String, Object> targetResolver) {
        AssignmentProducer producer = (target, value) -> {
            VariableScopeInstance variableScopeInstance = (VariableScopeInstance) nodeInstance.resolveContextInstance(VariableScope.VARIABLE_SCOPE, target);
            if (variableScopeInstance == null && nodeInstance != null) {
                nodeInstance.setVariable(target, value);
                return;
            }

            // proper inforation about the type
            Variable varDef = variableScopeInstance.getVariableScope().findVariable(target);
            DataType dataType = varDef.getType();

            // undefined or null we don't need to compute anything
            if (value == null || dataType instanceof UndefinedDataType) {
                if (nodeInstance != null) {
                    variableScopeInstance.setVariable(nodeInstance, target, value);
                } else {
                    variableScopeInstance.setVariable(target, value);
                }
                return;
            }

            // we try to convert with the converter
            // only if there is a TypeConverter registered for the data type
            if (value instanceof String) {
                value = dataType.readValue((String) value);
            }

            // the dataType is already the type
            if (dataType.verifyDataType(value)) {
                if (nodeInstance != null) {
                    variableScopeInstance.setVariable(nodeInstance, target, value);
                } else {
                    variableScopeInstance.setVariable(target, value);
                }
                return;
            }

            // if we use some strict variable this should not be needed but test require this.
            // this is some heuristics to try to transform stuff into the target type
            if (value != null && !(value instanceof Throwable)) {
                try {
                    if (!dataType.getStringType().endsWith("java.lang.Object") && dataType instanceof ObjectDataType) {
                        ClassLoader classLoader = ((ObjectDataType) dataType).getClassLoader();
                        if (classLoader != null) {
                            value = typeTransformer.transform(classLoader, value, dataType.getStringType());
                        } else {
                            value = typeTransformer.transform(value, dataType.getStringType());
                        }
                    } else if (!(dataType instanceof StringDataType) && !(dataType instanceof ObjectDataType)) {
                        value = typeTransformer.transform(value, dataType.getStringType());
                    }
                } catch (Exception e) {
                    logger.trace("error trying to transform value {}", value);
                }
            }

            if (!dataType.verifyDataType(value)) {
                if (dataType instanceof StringDataType) {
                    // last chance to put proper value
                    value = value.toString();
                } else {
                    throw new IllegalArgumentException("value " + value + " does not match " + dataType.getStringType());
                }
            }
            if (nodeInstance != null) {
                variableScopeInstance.setVariable(nodeInstance, target, value);
            } else {
                variableScopeInstance.setVariable(target, value);
            }
        };
        this.processDataAssociations((dataSet, mapping) -> dataSet, dataAssociation, sourceResolver, targetResolver, producer);
    }

    public void processOutputs(Collection<DataAssociation> dataAssociation, Function<String, Object> sourceResolver, Function<String, Object> targetResolver, AssignmentProducer producer) {
        this.processDataAssociations((dataSet, mapping) -> dataSet, dataAssociation, sourceResolver, targetResolver, producer);
    }

    private void processDataAssociations(
            BiFunction<Map<String, Object>, DataAssociation, Map<String, Object>> converter,
            Collection<DataAssociation> dataAssociation,
            Function<String, Object> sourceResolver,
            Function<String, Object> targetResolver,
            AssignmentProducer producer) {

        for (Iterator<DataAssociation> iterator = dataAssociation.iterator(); iterator.hasNext();) {
            DataAssociation mapping = iterator.next();
            Map<String, Object> sources = new HashMap<>();
            // this mapping it is only useful so we use the label name instead of id
            mapping.getSources().forEach(source -> {
                Object value = sourceResolver.apply(source.getLabel());
                sources.put(source.getLabel(), value);
            });
            processDataAssociation(converter, mapping, sources, sourceResolver, targetResolver, producer);
        }
    }

    private void processDataAssociation(
            BiFunction<Map<String, Object>, DataAssociation, Map<String, Object>> converter,
            DataAssociation mapping,
            Map<String, Object> dataSet,
            Function<String, Object> sourceResolver,
            Function<String, Object> targetResolver,
            AssignmentProducer producer) {
        try {
            if (mapping.getTransformation() != null) {
                Map<String, Object> transformationDataSet = null;
                // FIXME this is wrong for inputs as it should not require transforming to targets
                // transformations are applied over sources otherwise you only allow expressions over one single attribute which is not correct
                transformationDataSet = converter.apply(dataSet, mapping);
                Transformation transformation = mapping.getTransformation();
                DataTransformer transformer = DataTransformerRegistry.get().find(transformation.getLanguage());
                Object parameterValue = null;
                if (transformer != null) {
                    parameterValue = transformer.transform(transformation.getCompiledExpression(), transformationDataSet);
                }
                if (parameterValue != null) {
                    producer.accept(mapping.getTarget().getLabel(), parameterValue);
                }
            } else if (mapping.getAssignments() == null || mapping.getAssignments().isEmpty()) {
                // if no assignments copy source to target
                producer.accept(mapping.getTarget().getLabel(), dataSet.get(mapping.getSources().get(0).getLabel()));
            } else {

                mapping.getAssignments().forEach(a -> {
                    this.handleAssignment(a, sourceResolver, targetResolver, producer);
                });

            }
        } catch (Throwable th) {
            logger.debug("there was an error during data association processing", th);
            throw th;
        }
    }

    private void handleAssignment(Assignment assignment, Function<String, Object> sourceResolver, Function<String, Object> targetResolver, AssignmentProducer producer) {
        if (nodeInstance == null) {
            logger.debug("handle assignment is not possible for {}", assignment);
            return;
        }
        AssignmentAction action = (AssignmentAction) assignment.getMetaData("Action");
        try {
            KogitoProcessContextImpl context = new KogitoProcessContextImpl(nodeInstance.getProcessInstance().getKnowledgeRuntime());
            context.setNodeInstance(nodeInstance);
            action.execute(sourceResolver, targetResolver, producer);
        } catch (Exception e) {
            throw new RuntimeException("Unable to execute Assignment", e);
        }
    }

    public static Map<String, Object> processInputs(NodeInstanceImpl nodeInstanceImpl,
            Function<String, Object> sourceResolver,
            Function<String, Object> targetResolver) {

        Function<String, Object> varResolverWrapper = (varRef) -> {
            switch (varRef) {
                case "nodeInstance":
                    return nodeInstanceImpl;
                case "processInstance":
                    return nodeInstanceImpl.getProcessInstance();
                case "processInstanceId":
                    return nodeInstanceImpl.getKogitoProcessInstance().getStringId();
                case "parentProcessInstanceId":
                    return nodeInstanceImpl.getKogitoProcessInstance().getParentProcessInstanceStringId();
                default:
                    return sourceResolver.apply(varRef);
            }
        };

        // for inputs resolve it is supposed to create object by default constructor (that is the reason is null
        return processInputs(nodeInstanceImpl, ((NodeImpl) nodeInstanceImpl.getNode()).getInAssociations(), varResolverWrapper, targetResolver);
    }

    public static Map<String, Object> processInputs(NodeInstanceImpl nodeInstanceImpl,
            List<DataAssociation> dataInputAssociation,
            Function<String, Object> sourceResolver,
            Function<String, Object> targetResolver) {

        ElementIoHelper ioHelper = new ElementIoHelper(nodeInstanceImpl);
        Map<String, Object> inputSet = new HashMap<>();
        // for inputs resolve it is supposed to create object by default constructor (that is the reason is null
        ioHelper.processInputs(dataInputAssociation, sourceResolver, targetResolver, (target, value) -> inputSet.put(target, value));
        return inputSet;
    }

    public static Map<String, Object> processInputs(NodeInstanceImpl nodeInstanceImpl, Function<String, Object> soureResolver) {
        return processInputs(nodeInstanceImpl, soureResolver, key -> null);
    }

    public static void processOutputs(NodeInstanceImpl nodeInstanceImpl, Function<String, Object> sourceResolver, Function<String, Object> targetResolver) {
        processOutputs(nodeInstanceImpl, ((NodeImpl) nodeInstanceImpl.getNode()).getOutAssociations(), sourceResolver, targetResolver);
    }

    public static void processOutputs(NodeInstanceImpl nodeInstanceImpl,
            List<DataAssociation> dataOutputAssociations,
            Function<String, Object> sourceResolver,
            Function<String, Object> targetResolver) {

        ElementIoHelper ioHelper = new ElementIoHelper(nodeInstanceImpl);
        ioHelper.processOutputs(dataOutputAssociations, sourceResolver, targetResolver);
    }

    public static Map<String, Object> processOutputs(List<DataAssociation> dataOutputAssociations, Function<String, Object> sourceResolver) {
        ElementIoHelper ioHelper = new ElementIoHelper(null);
        Map<String, Object> outputSet = new HashMap<>();
        ioHelper.processOutputs(dataOutputAssociations, sourceResolver, key -> null, (key, value) -> outputSet.put(key, value));
        return outputSet;
    }

}
