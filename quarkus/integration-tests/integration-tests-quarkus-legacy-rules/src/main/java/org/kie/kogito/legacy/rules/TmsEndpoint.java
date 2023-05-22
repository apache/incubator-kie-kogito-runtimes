/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.legacy.rules;

import javax.inject.Inject;
import javax.ws.rs.*;

import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieRuntimeBuilder;
import org.kie.api.runtime.KieSession;

@Path("/test-tms")
public class TmsEndpoint {
    @Inject
    KieRuntimeBuilder kieRuntimeBuilder;

    @POST()
    public Object executeQuery(@QueryParam("string") String string) {
        return test(kieRuntimeBuilder, string);
    }

    public static Object test(KieRuntimeBuilder kieRuntimeBuilder, String string) {
        KieSession session = kieRuntimeBuilder.newKieSession();

        session.insert(string);
        session.fireAllRules();

        return session.getObjects(new ClassObjectFilter(Integer.class)).iterator().next();
    }
}
