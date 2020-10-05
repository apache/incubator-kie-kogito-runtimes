package org.kie.kogito.app;

import org.kie.kogito.event.Topic;
import org.kie.kogito.event.TopicType;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Path;

@Path("/messaging/topics")
public class TopicsInformationResource {

    private List<Topic> topics;

    public TopicsInformationResource() {
        topics = new ArrayList<>();
        /*
         * $repeat$
         * topics.add(new Topic("$name$", $type$));
         * $end_repeat$
         */
    }

    @GET()
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response getTopics() {
        return javax.ws.rs.core.Response.ok(topics).build();
    }

}