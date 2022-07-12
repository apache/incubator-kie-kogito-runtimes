/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.workflows.services;

import org.kie.kogito.examples.sw.greeting.Greeter;
import org.kie.kogito.examples.sw.greeting.Greeting.HelloReply;
import org.kie.kogito.examples.sw.greeting.Greeting.HelloReply.Builder;
import org.kie.kogito.examples.sw.greeting.Greeting.HelloReply.State;
import org.kie.kogito.examples.sw.greeting.Greeting.HelloRequest;
import org.kie.kogito.examples.sw.greeting.Greeting.InnerMessage;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

@GrpcService
public class GreeterService implements Greeter {
    @Override
    public Uni<HelloReply> sayHello(HelloRequest request) {
        String message;
        switch (request.getLanguage().toLowerCase()) {
            case "spanish":
                message = "Saludos desde gRPC service " + request.getName();
                break;
            case "english":
            default:
                message = "Hello from gRPC service " + request.getName();
        }
        Builder builder = HelloReply.newBuilder().setMessage(message);
        if (request.getInnerHello().getUnknown()) {
            builder.setState(State.UNKNOWN);
        } else {
            builder.setState(State.SUCCESS);
        }
        builder.setInnerMessage(InnerMessage.newBuilder().setNumber(23).build());
        return Uni.createFrom().item(() -> builder.build());
    }
}
