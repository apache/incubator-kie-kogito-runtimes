/*
 * Copyright (c) 2020. Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.integrationtests;

import java.io.IOException;

import org.drools.core.util.DroolsStreamUtils;
import org.kie.api.KieBase;
import org.kie.kogito.internal.runtime.KieSession;

public class SerializationHelper {

    public static <T> T serializeObject(final T obj) throws IOException,
            ClassNotFoundException {
        return serializeObject(obj, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T serializeObject(final T obj,
                                        final ClassLoader classLoader) throws IOException,
            ClassNotFoundException {
        return (T) DroolsStreamUtils.streamIn(DroolsStreamUtils.streamOut(obj), classLoader);
    }

    public static KieSession getSerialisedStatefulKnowledgeSession( final KieSession ksession,
                                                                                  final boolean dispose) throws Exception {
        return getSerialisedStatefulKnowledgeSession(ksession,
                dispose,
                true);
    }

    public static KieSession getSerialisedStatefulKnowledgeSession(final KieSession ksession,
                                                                                 final boolean dispose,
                                                                                 final boolean testRoundTrip) throws Exception {
        return getSerialisedStatefulKnowledgeSession(ksession, ksession.getKieBase(), dispose);
    }

    public static KieSession getSerialisedStatefulKnowledgeSession(final KieSession ksession,
                                                                                 final KieBase kbase,
                                                                                 final boolean dispose) throws Exception {
        return getSerialisedStatefulKnowledgeSessionWithMessage(ksession, kbase, dispose);
    }

    public static KieSession getSerialisedStatefulKnowledgeSessionWithMessage( final KieSession ksession,
                                                                                      final KieBase kbase,
                                                                                      final boolean dispose) throws Exception {
        throw new UnsupportedOperationException("Cannot marshall a non v7 session");
    }
}
