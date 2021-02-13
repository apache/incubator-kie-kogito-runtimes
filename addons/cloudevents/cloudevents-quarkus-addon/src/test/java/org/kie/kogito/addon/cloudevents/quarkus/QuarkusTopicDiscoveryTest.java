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

package org.kie.kogito.addon.cloudevents.quarkus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.kie.kogito.addon.cloudevents.AbstractTopicDiscovery;
import org.kie.kogito.event.ChannelType;
import org.kie.kogito.event.CloudEventMeta;
import org.kie.kogito.event.EventKind;
import org.kie.kogito.event.Topic;
import org.kie.kogito.event.TopicDiscovery;

import static org.assertj.core.api.Assertions.assertThat;

class QuarkusTopicDiscoveryTest {

    @Test
    @SetSystemProperty(key = "mp.messaging.outgoing.processedtravellers.connector", value = "smallrye-http")
    @SetSystemProperty(key = "mp.messaging.outgoing.processedtravellers.url", value = "http://localhost:8080/")
    @SetSystemProperty(key = "mp.messaging.incoming.kogito_incoming_stream.connector", value = "smallrye-kafka")
    @SetSystemProperty(key = "mp.messaging.incoming.kogito_incoming_stream.topic", value = "mycooltopic")
    void verifyTopicsWithPropertiesSet() {
        final List<Topic> expectedTopics = new ArrayList<>();
        expectedTopics.add(new Topic("processedtravellers", ChannelType.OUTGOING));
        expectedTopics.add(new Topic("mycooltopic", ChannelType.INCOMING));

        final TopicDiscovery discovery = new QuarkusTopicDiscovery();
        final List<Topic> topics = discovery.getTopics(Collections.emptyList());
        assertThat(topics).hasSize(2);
        expectedTopics.forEach(e -> assertThat(topics.stream().anyMatch(t -> t.getName().equals(e.getName()) && t.getType() == e.getType())).isTrue());
    }

    @Test
    @SetSystemProperty(key = "mp.messaging.outgoing.processedtravellers.connector", value = "smallrye-http")
    @SetSystemProperty(key = "mp.messaging.outgoing.processedtravellers.url", value = "http://localhost:8080/")
    @SetSystemProperty(key = "mp.messaging.outgoing.processedtravellers.topic", value = "mycooltopic")
    @SetSystemProperty(key = "mp.messaging.incoming.kogito_incoming_stream.connector", value = "smallrye-kafka")
    @SetSystemProperty(key = "mp.messaging.incoming.kogito_incoming_stream.topic", value = "mycooltopic")
    void verifyTopicsWithPropertiesSameTopic() {
        final List<Topic> expectedTopics = new ArrayList<>();
        expectedTopics.add(new Topic("mycooltopic", ChannelType.OUTGOING));
        expectedTopics.add(new Topic("mycooltopic", ChannelType.INCOMING));

        final TopicDiscovery discovery = new QuarkusTopicDiscovery();
        final List<Topic> topics = discovery.getTopics(Collections.emptyList());
        assertThat(topics).hasSize(2);
        expectedTopics.forEach(e -> assertThat(topics.stream().anyMatch(t -> t.getName().equals(e.getName()) && t.getType() == e.getType())).isTrue());
    }

    @Test
    @ClearSystemProperty(key = "mp.messaging.outgoing.processedtravellers.connector")
    @ClearSystemProperty(key = "mp.messaging.outgoing.processedtravellers.url")
    @ClearSystemProperty(key = "mp.messaging.outgoing.processedtravellers.topic")
    @ClearSystemProperty(key = "mp.messaging.incoming.kogito_incoming_stream.connector")
    @ClearSystemProperty(key = "mp.messaging.incoming.kogito_incoming_stream.topic")
    void verifyTopicsWithNoPropertiesSet() {
        final List<Topic> expectedTopics = new ArrayList<>();
        expectedTopics.add(AbstractTopicDiscovery.DEFAULT_OUTGOING_CHANNEL);
        expectedTopics.add(AbstractTopicDiscovery.DEFAULT_INCOMING_CHANNEL);
        final List<CloudEventMeta> eventsMeta = new ArrayList<>();
        eventsMeta.add(new CloudEventMeta("event1", "", EventKind.CONSUMED));
        eventsMeta.add(new CloudEventMeta("event2", "", EventKind.PRODUCED));

        final TopicDiscovery discovery = new QuarkusTopicDiscovery();
        final List<Topic> topics = discovery.getTopics(eventsMeta);
        assertThat(topics).hasSize(2);
        expectedTopics.forEach(e -> assertThat(topics.stream().anyMatch(t -> t.getName().equals(e.getName()) && t.getType() == e.getType())).isTrue());
    }

    @Test
    @ClearSystemProperty(key = "mp.messaging.outgoing.processedtravellers.connector")
    @ClearSystemProperty(key = "mp.messaging.outgoing.processedtravellers.url")
    @ClearSystemProperty(key = "mp.messaging.outgoing.processedtravellers.topic")
    @ClearSystemProperty(key = "mp.messaging.incoming.kogito_incoming_stream.connector")
    @ClearSystemProperty(key = "mp.messaging.incoming.kogito_incoming_stream.topic")
    void verifyTopicsWithPropertiesAndChannels() {
        final TopicDiscovery discovery = new QuarkusTopicDiscovery();
        final List<Topic> topics = discovery.getTopics(Collections.emptyList());
        assertThat(topics).isEmpty();
    }
}