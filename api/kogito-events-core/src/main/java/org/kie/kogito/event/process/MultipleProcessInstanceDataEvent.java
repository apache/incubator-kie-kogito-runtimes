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
package org.kie.kogito.event.process;

import java.net.URI;
import java.util.Collection;

public class MultipleProcessInstanceDataEvent extends ProcessInstanceDataEvent<Collection<ProcessInstanceDataEvent<? extends KogitoMarshallEventSupport>>> {

    public static final String MULTIPLE_TYPE = "MultipleProcessInstanceDataEvent";
    public static final String BINARY_CONTENT_TYPE = "application/octet-stream";
    public static final String COMPRESS_DATA = "CompressData";

    public MultipleProcessInstanceDataEvent() {
    }

    public MultipleProcessInstanceDataEvent(URI source, Collection<ProcessInstanceDataEvent<? extends KogitoMarshallEventSupport>> body) {
        super(MULTIPLE_TYPE, source, body);
    }

    public boolean isCompressed() {
        Object extension = getExtension(MultipleProcessInstanceDataEvent.COMPRESS_DATA);
        return extension instanceof Boolean ? ((Boolean) extension).booleanValue() : false;
    }

    public void setCompressed(boolean compressed) {
        addExtensionAttribute(COMPRESS_DATA, compressed);
    }

}
