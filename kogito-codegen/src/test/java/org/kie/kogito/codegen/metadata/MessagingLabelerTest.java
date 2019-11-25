/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.codegen.metadata;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MessagingLabelerTest {

    @Test
    public void getLabelsFromApplicationWithTopics() throws URISyntaxException {
        final MessagingLabeler labeler = new MessagingLabeler(new File(this.getClass().getResource("/labels/messaging/has_properties").toURI().getPath()));
        final Map<String, String> labels = labeler.generateLabels();
        final String topics[] = "kogito-usertaskinstances-events,kogito-processinstances-events,visasapproved,visasrejected,visaapplications".split(",");
        assertThat(labels).isNotNull();
        assertThat(labels).containsKey(MessagingLabeler.MESSAGING_LABEL_PREFIX);
        assertThat(labels).hasSize(1);
        assertThat(labels.get(MessagingLabeler.MESSAGING_LABEL_PREFIX).split(",")).contains(topics);
    }

    @Test
    public void getLabelsFromApplicationNoClassDir() throws URISyntaxException {
        // path doesn't exist
        final MessagingLabeler labeler = new MessagingLabeler(new File(this.getClass().getResource("/labels").toURI().getPath()));
        final Map<String, String> labels = labeler.generateLabels();
        assertThat(labels).isNotNull();
        assertThat(labels).hasSize(0);
    }

    @Test
    public void getLabelsFromApplicationEmptyProperties() throws URISyntaxException {
        // path doesn't exist
        final MessagingLabeler labeler = new MessagingLabeler(new File(this.getClass().getResource("/labels/messaging/empty_properties").toURI().getPath()));
        final Map<String, String> labels = labeler.generateLabels();
        assertThat(labels).isNotNull();
        assertThat(labels).hasSize(0);
    }
}