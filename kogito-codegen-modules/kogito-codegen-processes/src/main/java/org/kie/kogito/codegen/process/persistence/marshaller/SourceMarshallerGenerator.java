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
package org.kie.kogito.codegen.process.persistence.marshaller;

import java.util.Collection;
import java.util.Optional;

import org.infinispan.protostream.descriptors.FieldDescriptor;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public class SourceMarshallerGenerator extends AbstractMarshallerGenerator<ClassOrInterfaceDeclaration> {

    public SourceMarshallerGenerator(KogitoBuildContext context, Collection<ClassOrInterfaceDeclaration> rawDataClasses) {
        super(context, rawDataClasses);
    }

    public SourceMarshallerGenerator(KogitoBuildContext context) {
        this(context, null);
    }

    @Override
    protected boolean isArray(String javaType, FieldDescriptor field) {
        Optional<ClassOrInterfaceDeclaration> clazz = modelClasses.stream().filter(cls -> cls.getName().equals(javaType)).findFirst();
        if (clazz.isPresent()) {
            try {
                // TODO gcardosi 1460
                return false;
                //                PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz.get()).getPropertyDescriptors();
                //                for (PropertyDescriptor pd : pds) {
                //                    if (pd.getName().equals(field.getName())) {
                //                        return pd.getPropertyType().isArray();
                //                    }
                //                }
                //                return false;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
