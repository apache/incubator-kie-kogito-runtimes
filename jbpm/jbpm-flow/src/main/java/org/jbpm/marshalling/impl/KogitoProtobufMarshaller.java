/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.marshalling.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.drools.core.SessionConfiguration;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.marshalling.impl.MarshallingConfigurationImpl;
import org.drools.core.marshalling.impl.RuleBaseNodes;
import org.drools.serialization.protobuf.ProtobufInputMarshaller;
import org.drools.serialization.protobuf.ProtobufMarshaller;
import org.drools.serialization.protobuf.ProtobufOutputMarshaller;
import org.drools.serialization.protobuf.ReadSessionResult;
import org.kie.kogito.internal.KieBase;
import org.kie.kogito.internal.marshalling.MarshallingConfiguration;
import org.kie.kogito.internal.runtime.Environment;
import org.kie.kogito.internal.runtime.KieSession;
import org.kie.kogito.internal.runtime.KieSessionConfiguration;


/**
 * A Marshaller implementation that uses ProtoBuf as the marshalling
 * framework in order to support backward compatibility with
 * marshalled sessions
 * 
 */
public class KogitoProtobufMarshaller extends ProtobufMarshaller {

    public KogitoProtobufMarshaller( KieBase kbase,
                                     MarshallingConfigurationImpl marshallingConfig) {
        super(null, marshallingConfig);
    }

    @Override
    public StatefulKnowledgeSessionImpl unmarshall( final InputStream stream) throws IOException,
            ClassNotFoundException {
        return unmarshall( stream, null, null );
    }

    @Override
    public StatefulKnowledgeSessionImpl unmarshall( final InputStream stream,
                                                KieSessionConfiguration config,
                                                Environment environment) throws IOException, ClassNotFoundException {
        return unmarshallWithMessage(stream, config, environment).getSession();
    }

    public void unmarshall(final InputStream stream,
                           final KieSession ksession) throws IOException, ClassNotFoundException {
        KogitoMarshallerReaderContext context = getMarshallerReaderContext(stream, ksession.getEnvironment());
        ProtobufInputMarshaller.readSession(( StatefulKnowledgeSessionImpl ) ksession, context);
        context.close();
    }

    public void marshall(final OutputStream stream,
                         final KieSession ksession) throws IOException {
        marshall( stream, ksession, ksession.getSessionClock().getCurrentTime() );
    }

    public void marshall(final OutputStream stream,
                         final KieSession ksession,
                         final long clockTime) throws IOException {
        (( InternalWorkingMemory ) ksession).flushPropagations();
        KogitoMarshallerWriteContext context = new KogitoMarshallerWriteContext( stream,
                                                                     ( InternalKnowledgeBase ) kbase,
                                                                     ( InternalWorkingMemory ) ksession,
                                                                     RuleBaseNodes.getNodeMap( ( InternalKnowledgeBase ) kbase ),
                                                                     this.strategyStore,
                                                                     this.marshallingConfig.isMarshallProcessInstances(),
                                                                     this.marshallingConfig.isMarshallWorkItems(),
                                                                     ksession.getEnvironment() );
        context.setClockTime( clockTime );
        ProtobufOutputMarshaller.writeSession( context );
        context.close();
    }

    @Override
    public MarshallingConfiguration getMarshallingConfiguration() {
        return marshallingConfig;
    }

    @Override
    public ReadSessionResult unmarshallWithMessage( final InputStream stream,
                                                    KieSessionConfiguration config,
                                                    Environment environment) throws IOException, ClassNotFoundException {
        KogitoMarshallerReaderContext context = getMarshallerReaderContext(stream, environment);
        int id = (( KnowledgeBaseImpl ) this.kbase).nextWorkingMemoryCounter();
        ReadSessionResult readSessionResult = ProtobufInputMarshaller.readSession(context,
                                                                                  id,
                                                                                  environment,
                                                                                  ( SessionConfiguration ) config,
                                                                                  initializer);
        context.close();
        if ( (( SessionConfiguration ) config).isKeepReference() ) {
            (( KnowledgeBaseImpl ) this.kbase).addStatefulSession(readSessionResult.getSession());
        }
        return readSessionResult;
    }

    private KogitoMarshallerReaderContext getMarshallerReaderContext( final InputStream inputStream, final Environment environment) throws IOException {
        return new KogitoMarshallerReaderContext(inputStream,
                                           ( KnowledgeBaseImpl ) kbase,
                                           RuleBaseNodes.getNodeMap(( KnowledgeBaseImpl ) kbase),
                                           this.strategyStore,
                                           TIMER_READERS,
                                           this.marshallingConfig.isMarshallProcessInstances(),
                                           this.marshallingConfig.isMarshallWorkItems(),
                                           environment);
    }

}
