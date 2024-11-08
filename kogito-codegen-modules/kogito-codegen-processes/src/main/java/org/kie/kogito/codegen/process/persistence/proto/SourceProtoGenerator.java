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
package org.kie.kogito.codegen.process.persistence.proto;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.drools.codegen.common.GeneratedFile;
import org.infinispan.protostream.annotations.ProtoEnumValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public class SourceProtoGenerator extends AbstractProtoGenerator<ClassOrInterfaceDeclaration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceProtoGenerator.class);

    private SourceProtoGenerator(Collection<ClassOrInterfaceDeclaration> modelClasses, Collection<ClassOrInterfaceDeclaration> dataClasses) {
        super(modelClasses, dataClasses);
    }

    @Override
    protected boolean isEnum(ClassOrInterfaceDeclaration dataModel) {
        // TODO gcardosi 1460
        return false; //dataModel.isEnum();
    }

    @Override
    protected ProtoMessage messageFromClass(Proto proto, Set<String> alreadyGenerated, ClassOrInterfaceDeclaration clazz, String messageComment, String fieldComment) throws Exception {
        if (!shouldGenerateProto(clazz)) {
            LOGGER.info("Skipping generating reflection proto for class {}", clazz);
            return null;
        }
        LOGGER.debug("Generating reflection proto for class {}", clazz);

        String clazzName = extractName(clazz).get();
        // TODO gcardosi 1460
        return null;
        //        ProtoMessage message = new ProtoMessage(clazzName, clazz.getPackage().getName());
        //        Predicate<PropertyDescriptor> validPropertyFilter = property -> this.isValidProperty(clazz, property);
        //        List<PropertyDescriptor> propertiesDescriptor = List.of(Introspector.getBeanInfo(clazz).getPropertyDescriptors()).stream().filter(validPropertyFilter).toList();
        //        for (PropertyDescriptor pd : propertiesDescriptor) {
        //
        //            Field propertyField = getFieldFromClass(clazz, pd.getName());
        //
        //            // By default, only index id field from Model generated class
        //            String completeFieldComment = "id".equals(pd.getName()) && Model.class.isAssignableFrom(clazz) ? fieldComment.replace("Index.NO", "Index.YES") : fieldComment;
        //
        //            VariableInfo varInfo = propertyField.getAnnotation(VariableInfo.class);
        //            if (varInfo != null) {
        //                completeFieldComment = fieldComment + "\n @VariableInfo(tags=\"" + varInfo.tags() + "\")";
        //            }
        //
        //            String fieldTypeString = pd.getPropertyType().getCanonicalName();
        //            Class fieldType = pd.getPropertyType();
        //            String protoType;
        //            if (pd.getPropertyType().isArray() && !pd.getPropertyType().getComponentType().isPrimitive()) {
        //                fieldTypeString = ARRAY;
        //                fieldType = pd.getPropertyType().getComponentType();
        //                protoType = protoType(fieldType.getCanonicalName());
        //            } else if (Collection.class.isAssignableFrom(pd.getPropertyType())) {
        //                fieldTypeString = COLLECTION;
        //                Type type = propertyField.getGenericType();
        //                if (type instanceof ParameterizedType) {
        //                    ParameterizedType ptype = (ParameterizedType) type;
        //                    fieldType = (Class) ptype.getActualTypeArguments()[0];
        //                    protoType = protoType(fieldType.getCanonicalName());
        //                } else {
        //                    throw new IllegalArgumentException("Field " + propertyField.getName() + " of class " + clazz.getName() + " uses collection without type information");
        //                }
        //            } else {
        //                protoType = protoType(fieldTypeString);
        //            }
        //
        //            if (protoType == null) {
        //
        //                // recursive call to visit the type
        //                Optional<String> optionalProtoType = internalGenerate(proto, alreadyGenerated, messageComment, fieldComment, fieldType);
        //                if (!optionalProtoType.isPresent()) {
        //                    return message;
        //                }
        //
        //                protoType = optionalProtoType.get();
        //            }
        //
        //            ProtoField protoField = message.addField(computeCardinalityModifier(fieldTypeString), protoType, pd.getName());
        //            protoField.setComment(completeFieldComment);
        //            if (KOGITO_SERIALIZABLE.equals(protoType)) {
        //                protoField.setOption(format("[(%s) = \"%s\"]", KOGITO_JAVA_CLASS_OPTION, pd.getPropertyType().getCanonicalName()));
        //            }
        //        }
        //        message.setComment(messageComment);
        //        proto.addMessage(message);
        //        return message;
    }

    protected boolean shouldGenerateProto(ClassOrInterfaceDeclaration clazz) {
        return extractName(clazz).isPresent();
    }

    @Override
    protected Optional<String> extractName(ClassOrInterfaceDeclaration clazz) {
        try {
            // TODO gcardosi 1460
            return Optional.empty();
            //            clazz.findAll()
            //            // builtins should not generate proto files
            //            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            //            String name = beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
            //
            //            Predicate<String> typeExclusions = ExclusionTypeUtils.createTypeExclusions();
            //            if (typeExclusions.test(clazz.getCanonicalName())) {
            //                return Optional.empty();
            //            }
            //            Generated generatedData = clazz.getAnnotation(Generated.class);
            //            if (generatedData != null) {
            //                name = generatedData.name().isEmpty() ? name : generatedData.name();
            //                if (generatedData.hidden()) {
            //                    // since class is marked as hidden skip processing of that class
            //                    return Optional.empty();
            //                }
            //            }
            //            return Optional.of(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected String modelClassName(ClassOrInterfaceDeclaration dataModel) {
        // TODO gcardosi 1460
        return "";
        //return dataModel.getName();
    }

    //    private Field getFieldFromClass(ClassOrInterfaceDeclaration clazz, String name) {
    //        try {
    //            return clazz.getDeclaredField(name);
    //        } catch (Exception e) {
    //            if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
    //                return getFieldFromClass(clazz.getSuperclass(), name);
    //            } else {
    //                throw new IllegalArgumentException("Impossible to find field " + name + " in class " + clazz.getName());
    //            }
    //        }
    //    }

    @Override
    protected ProtoEnum enumFromClass(Proto proto, ClassOrInterfaceDeclaration clazz) throws Exception {
        try {
            // TODO gcardosi 1460
            return null;
            //            return extractName(clazz)
            //                    .map(name -> {
            //                        ProtoEnum modelEnum = new ProtoEnum(name, clazz.getPackage().getName());
            //                        Stream.of(clazz.getDeclaredFields())
            //                                .filter(Field::isEnumConstant)
            //                                .sorted(Comparator.comparing(Field::getName))
            //                                .forEach(f -> addEnumField(f, modelEnum));
            //                        proto.addEnum(modelEnum);
            //                        return modelEnum;
            //                    }).orElse(null);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed class " + clazz.getName() + " " + e.getMessage(), e);
        }
    }

    private void addEnumField(Field field, ProtoEnum pEnum) {
        ProtoEnumValue protoEnumValue = field.getAnnotation(ProtoEnumValue.class);
        Integer ordinal = null;
        boolean sortedWithAnnotation = false;
        if (protoEnumValue != null) {
            sortedWithAnnotation = true;
            ordinal = protoEnumValue.number();
        }
        if (ordinal == null) {
            ordinal = Enum.valueOf((Class<Enum>) field.getType(), field.getName()).ordinal();
        }
        pEnum.addField(field.getName(), ordinal, sortedWithAnnotation);
    }

    @Override
    protected Optional<GeneratedFile> generateModelClassProto(ClassOrInterfaceDeclaration modelClazz) {
        // TODO gcardosi 1460
        //        Generated generatedData = modelClazz.getAnnotation(Generated.class);
        //        if (generatedData != null) {
        //
        //            String processId = generatedData.reference();
        //            Proto modelProto = generate("@Indexed",
        //                    INDEX_COMMENT,
        //                    modelClazz.getPackage().getName() + "." + processId, modelClazz,
        //                    "import \"kogito-index.proto\";",
        //                    "import \"kogito-types.proto\";",
        //                    "option kogito_model = \"" + generatedData.name() + "\";",
        //                    "option kogito_id = \"" + processId + "\";");
        //            if (modelProto.getMessages().isEmpty()) {
        //                // no messages, nothing to do
        //                return Optional.empty();
        //            }
        //            ProtoMessage modelMessage = modelProto.getMessages().stream().filter(msg -> msg.getName().equals(generatedData.name())).findFirst()
        //                    .orElseThrow(() -> new IllegalStateException("Unable to find model message"));
        //            modelMessage.addField("optional", "org.kie.kogito.index.model.KogitoMetadata", "metadata").setComment(INDEX_COMMENT);
        //
        //            return Optional.of(generateProtoFiles(processId, modelProto));
        //        }
        return Optional.empty();
    }

    public static Builder<ClassOrInterfaceDeclaration, SourceProtoGenerator> builder() {
        return new SourceProtoGeneratorBuilder();
    }

    private static class SourceProtoGeneratorBuilder extends AbstractProtoGeneratorBuilder<ClassOrInterfaceDeclaration, SourceProtoGenerator> {

        private static final Logger LOGGER = LoggerFactory.getLogger(SourceProtoGeneratorBuilder.class);

        private SourceProtoGeneratorBuilder() {
        }

        @Override
        protected Collection<ClassOrInterfaceDeclaration> extractDataClasses(Collection<ClassOrInterfaceDeclaration> modelClasses) {
            // TODO gcardosi 1460
            return Collections.EMPTY_SET;

            //            if (dataClasses != null || modelClasses == null) {
            //                LOGGER.info("Using provided dataClasses instead of extracting from modelClasses. This should happen only during tests.");
            //                return dataClasses;
            //            }
            //            Set<ClassOrInterfaceDeclaration> dataModelClasses = new HashSet<>();
            //            try {
            //                for (ClassOrInterfaceDeclaration modelClazz : modelClasses) {
            //                    BeanInfo beanInfo = Introspector.getBeanInfo(modelClazz);
            //                    for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            //                        Class propertyType = pd.getPropertyType();
            //                        if (propertyType.getCanonicalName().startsWith("java.lang")
            //                                || propertyType.getCanonicalName().equals(Date.class.getCanonicalName())
            //                                || propertyType.isPrimitive()
            //                                || propertyType.isInterface()) {
            //                            continue;
            //                        }
            //
            //                        dataModelClasses.add(propertyType);
            //                    }
            //                }
            //                return dataModelClasses;
            //            } catch (IntrospectionException e) {
            //                throw new IllegalStateException("Error during bean introspection", e);
            //            }
        }

        @Override
        public SourceProtoGenerator build(Collection<ClassOrInterfaceDeclaration> modelClasses) {
            return new SourceProtoGenerator(modelClasses, extractDataClasses(modelClasses));
        }
    }

    //    private boolean isValidProperty(ClassOrInterfaceDeclaration clazz, PropertyDescriptor propertyDescriptor) {
    //        try {
    //            if (propertyDescriptor.getName().equals("class")) {
    //                return false;
    //            }
    //
    //            Field propertyField = getFieldFromClass(clazz, propertyDescriptor.getName());
    //
    //            // ignore static and/or transient fields
    //            int mod = propertyField.getModifiers();
    //            if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
    //                return false;
    //            }
    //
    //            return true;
    //        } catch (IllegalArgumentException ex) {
    //            LOGGER.warn(ex.getMessage());
    //            // a method starting with get or set without a corresponding backing field makes java beans to
    //            // still generate a property descriptor, it should be ignored
    //            return false;
    //        }
    //    }
}
