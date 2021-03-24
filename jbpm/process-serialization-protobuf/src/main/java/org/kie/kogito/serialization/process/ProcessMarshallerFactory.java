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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.kie.api.runtime.Environment;
import org.kie.kogito.serialization.process.marshaller.KogitoMarshallerReaderContext;
import org.kie.kogito.serialization.process.marshaller.KogitoProcessInstanceMarshaller;
import org.kie.kogito.serialization.process.marshaller.KogitoProcessMarshallerWriteContext;

/**
 * Registry for Process/ProcessMarshaller
 */
public class ProcessMarshallerFactory {

    public static final String FORMAT = "FORMAT";

    public static ProcessMarshallerFactory INSTANCE = new ProcessMarshallerFactory();

    public static ProcessInstanceMarshaller newKogitoProcessInstanceMarshaller() {
        return KogitoProcessInstanceMarshaller.INSTANCE;
    }

    public static MarshallerWriterContext newWriterContext(OutputStream baos, Environment env) throws IOException {
        return new KogitoProcessMarshallerWriteContext(env, baos);
    }

    public static MarshallerReaderContext newReaderContext(InputStream bais, Environment env) throws IOException {
        return new KogitoMarshallerReaderContext(env, bais);
    }

}
