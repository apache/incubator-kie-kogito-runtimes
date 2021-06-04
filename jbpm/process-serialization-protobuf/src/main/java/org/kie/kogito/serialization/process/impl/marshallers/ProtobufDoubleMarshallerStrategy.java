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

package org.kie.kogito.serialization.process.impl.marshallers;

import org.kie.kogito.serialization.process.ObjectMarshallerStrategy;
import org.kie.kogito.serialization.process.ProcessInstanceMarshallerException;

import com.google.protobuf.Any;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.InvalidProtocolBufferException;

public class ProtobufDoubleMarshallerStrategy implements ObjectMarshallerStrategy {

    @Override
    public boolean acceptForMarshalling(Object value) {
        return Double.class.equals(value.getClass());
    }

    @Override
    public boolean acceptForUnmarshalling(Object value) {
        return ((Any) value).is(DoubleValue.class);
    }

    @Override
    public Object marshall(Object unmarshalled) {
        return Any.pack(DoubleValue.of((Double) unmarshalled));
    }

    @Override
    public Object unmarshall(Object marshalled) {
        try {
            Any data = (Any) marshalled;
            DoubleValue storedValue = data.unpack(DoubleValue.class);
            return storedValue.getValue();
        } catch (InvalidProtocolBufferException e1) {
            throw new ProcessInstanceMarshallerException("Error trying to unmarshalling a double value", e1);
        }
    }

}
