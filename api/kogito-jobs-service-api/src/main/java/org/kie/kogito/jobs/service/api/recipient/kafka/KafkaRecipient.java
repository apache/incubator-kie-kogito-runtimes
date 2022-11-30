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

package org.kie.kogito.jobs.service.api.recipient.kafka;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.kie.kogito.jobs.service.api.Recipient;

@Schema(description = "Recipient definition to execute a job with a kafka message", allOf = { Recipient.class })
public class KafkaRecipient extends Recipient<byte[]> {

    private String bootstrapServers;
    private String topicName;
    private Map<String, String> headers;

    public KafkaRecipient() {
        this.headers = new HashMap<>();
    }

    public KafkaRecipient(byte[] payload,
            String bootstrapServers,
            String topicName,
            Map<String, String> headers) {
        super(payload);
        this.bootstrapServers = bootstrapServers;
        this.topicName = topicName;
        this.headers = headers != null ? headers : new HashMap<>();
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers != null ? headers : new HashMap<>();
    }

    @Override
    public String toString() {
        return "KafkaRecipient{" +
                "bootstrapServers='" + bootstrapServers + '\'' +
                ", topicName='" + topicName + '\'' +
                ", headers=" + headers +
                "} " + super.toString();
    }
}
