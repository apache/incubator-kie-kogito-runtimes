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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * Generates labels for messaging add-ons.
 * This {@link Labeler} will scan the target directory for the application.properties file
 * and will read every property that starts with <code>mp.messaging.incoming.</code> or <code>mp.messaging.outcoming.</code>.
 * Those properties values are going to be added to the image metadata to be read by cloud components.
 *
 * @see <a href="https://github.com/kiegroup/kogito-runtimes/wiki/Messaging">Kogito Runtimes - Messaging</a>
 */
public class MessagingLabeler implements Labeler {

    static final String MESSAGING_LABEL_PREFIX = ImageMetaData.LABEL_PREFIX + "messaging/topics";
    private static final String CLASSES_DIR = "classes";
    private static final String PROPS_FILE = "application.properties";
    private static final String KEY_REGEXP_PATTERN = "mp\\.messaging\\.(.+)\\.topic";
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingLabeler.class);

    private final File targetDirectory;
    private final Map<String, String> labels = new HashMap<>();

    public MessagingLabeler(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Override
    public Map<String, String> generateLabels() {
        if (targetDirectory != null && targetDirectory.isDirectory()) {
            final Path propertiesPath = Paths.get(targetDirectory.getPath(), CLASSES_DIR, PROPS_FILE);
            if (propertiesPath.toFile().exists()) {
                try (InputStream propsFile = Files.newInputStream(propertiesPath)) {
                    StringJoiner sj = new StringJoiner(",");
                    Properties applicationProperties = new Properties();
                    applicationProperties.load(propsFile);
                    applicationProperties
                            .entrySet()
                            .stream()
                            .filter(p -> ((String) p.getKey()).matches(KEY_REGEXP_PATTERN))
                            .forEach(p -> sj.add(p.getValue().toString()));
                    if (sj.length() > 0) {
                        LOGGER.debug("Adding topics to the labels: {}", sj);
                        labels.put(MESSAGING_LABEL_PREFIX, sj.toString());
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("Error while reading application.properties file", e);
                }
            }
        }
        return labels;
    }

}
