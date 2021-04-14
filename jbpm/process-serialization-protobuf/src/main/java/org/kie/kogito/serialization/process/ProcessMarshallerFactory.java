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
package org.kie.kogito.serialization.process;

import java.io.InputStream;
import java.io.OutputStream;

import org.kie.kogito.serialization.process.marshaller.KogitoMarshallerReaderContext;
import org.kie.kogito.serialization.process.marshaller.KogitoProcessInstanceMarshaller;
import org.kie.kogito.serialization.process.marshaller.KogitoProcessMarshallerWriteContext;

/**
 * Registry for Process/ProcessMarshaller
 */
public class ProcessMarshallerFactory {

    private ProcessMarshallerFactory() {
        // do nothing
    }

    public static ProcessInstanceMarshaller newKogitoProcessInstanceMarshaller() {
        return new KogitoProcessInstanceMarshaller();
    }

    public static MarshallerWriterContext newWriterContext(OutputStream baos) {
        return new KogitoProcessMarshallerWriteContext(baos);
    }

    public static MarshallerReaderContext newReaderContext(InputStream bais) {
        return new KogitoMarshallerReaderContext(bais);
    }

}
