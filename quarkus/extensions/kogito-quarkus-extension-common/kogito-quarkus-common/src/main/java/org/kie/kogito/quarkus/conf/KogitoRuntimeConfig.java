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
package org.kie.kogito.quarkus.conf;

import java.util.Optional;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.Recorder;

@Singleton
@Recorder
public class KogitoRuntimeConfig {

    @ConfigProperty(name = "kogito.service.url")
    Optional<String> kogitoServiceUrl;

    @ConfigProperty(name = "kogito.messaging.as-cloudevents", defaultValue = "true")
    boolean useCloudEvents;

    @ConfigProperty(name = "kogito.jackson.fail-on-empty-bean", defaultValue = "false")
    boolean failOnEmptyBean;

    @ConfigProperty(name = "quarkus.application.name")
    java.util.Optional<String> applicationName;

    @ConfigProperty(name = "quarkus.application.version")
    java.util.Optional<String> applicationVersion;

    private static String groupId = "";

    @Inject
    ConfigBean kogitoQuarkusConfigBean;

    void startup(@Observes StartupEvent event) {
        kogitoQuarkusConfigBean.setupConfigBean(kogitoServiceUrl.orElse(""), useCloudEvents, failOnEmptyBean,
                groupId, applicationName.orElse(""), applicationVersion.orElse(""));
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
