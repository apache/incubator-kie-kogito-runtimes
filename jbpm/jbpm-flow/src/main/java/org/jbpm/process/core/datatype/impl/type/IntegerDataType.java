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
package org.jbpm.process.core.datatype.impl.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jbpm.process.core.datatype.DataType;

/**
 * Representation of an integer datatype.
 */
public class IntegerDataType implements DataType {

    private static final long serialVersionUID = 510l;

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
    }

    @Override
    public boolean verifyDataType(final Object value) {
        return value == null || value instanceof Integer;
    }

    @Override
    public Object readValue(String value) {
        return Integer.valueOf(value);
    }

    @Override
    public String writeValue(Object value) {
        Integer i = (Integer) value;
        return i == null ? "" : i.toString();
    }

    @Override
    public String getStringType() {
        return "java.lang.Integer";
    }

    @Override
    public Class<?> getObjectClass() {
        return Integer.class;
    }

}
