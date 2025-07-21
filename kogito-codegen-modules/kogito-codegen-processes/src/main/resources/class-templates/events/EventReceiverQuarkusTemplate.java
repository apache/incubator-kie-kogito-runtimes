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
package $Package$;

import java.util.concurrent.CompletionStage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;


import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import org.kie.kogito.process.Processes;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.SignalFactory;
import org.kie.kogito.event.EventReceiver;

import java.util.concurrent.CompletableFuture;
import jakarta.inject.Inject;

@ApplicationScoped
public class $Trigger$EventReceiver implements EventReceiver {

    @Inject
    Processes processes;

    @Incoming("$Trigger$")
    public CompletionStage<?> onEvent(Message<$Type$> message) {
        try {
            for (String processId : processes.processIds()) {
                processes.processById(processId).send(SignalFactory.of("$Trigger$", message.getPayload()));
            }
            return CompletableFuture.completedStage(null);
        } catch (Throwable th) {
            return CompletableFuture.failedStage(th);
        }
    }

}
