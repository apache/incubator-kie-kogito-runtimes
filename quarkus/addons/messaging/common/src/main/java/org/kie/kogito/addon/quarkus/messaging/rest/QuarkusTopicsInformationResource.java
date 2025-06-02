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
package org.kie.kogito.addon.quarkus.messaging.rest;

import java.util.List;

import org.kie.kogito.event.Topic;
import org.kie.kogito.event.TopicDiscovery;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/messaging/topics")
@ApplicationScoped()
public class QuarkusTopicsInformationResource {

    @Inject
    private Instance<TopicDiscovery> topicDiscovery;

    @GET()
    @Produces(MediaType.APPLICATION_JSON)
    public jakarta.ws.rs.core.Response getTopics() {
        List<Topic> topics = topicDiscovery.stream().map(TopicDiscovery::getTopics).flatMap(List::stream).toList();
        return jakarta.ws.rs.core.Response.ok(topics).build();
    }
}
