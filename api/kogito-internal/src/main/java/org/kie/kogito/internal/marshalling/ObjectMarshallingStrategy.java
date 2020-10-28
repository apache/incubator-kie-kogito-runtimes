/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.internal.marshalling;

import java.io.IOException;
import java.io.ObjectInputStream;

public interface ObjectMarshallingStrategy extends org.kie.api.marshalling.ObjectMarshallingStrategy {

    /**
     * This method is analogous to the read method, but instead of reading it from an
     * input stream, it reads it from a byte[]
     *
     * @param context the context for this strategy created by the method #createContext()
     * @param object the marshalled object in a byte[]
     *
     * @return the unmarshalled Object
     */
    Object unmarshal( String dataType,
                      Context context,
                      ObjectInputStream is,
                      byte[] object,
                      ClassLoader classloader ) throws IOException, ClassNotFoundException;

    @Override
    default Object unmarshal( Context context,
                              ObjectInputStream is,
                              byte[] object,
                              ClassLoader classloader ) throws IOException, ClassNotFoundException {
        return unmarshal( null, context, is, object, classloader );
    }
    
    
    default String getType(Class<?> clazz) {
        return clazz.getCanonicalName();
    }

}

