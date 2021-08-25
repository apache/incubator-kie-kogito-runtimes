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
package org.kie.kogito.integrationtests;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.kie.kogito.incubation.common.DataContext;
import org.kie.kogito.incubation.common.MapDataContext;
import org.kie.kogito.incubation.processes.ProcessId;
import org.kie.kogito.incubation.processes.services.StraightThroughProcessService;

@Path("/custom")
public class CustomHelloResource {

    @Inject
    StraightThroughProcessService processService;

    //    @Channel("hello-publisher")
    //    Emitter<User> emitter;

    @POST
    public DataContext post(User user) {
        MapDataContext context = MapDataContext.create();
        context.set("user", user);
        return processService.evaluate(new ProcessId("hello"), context);
    }

    @POST
    @Path("/message")
    public User publish(User user) {
        //        emitter.send(user);
        return user;
    }
}
