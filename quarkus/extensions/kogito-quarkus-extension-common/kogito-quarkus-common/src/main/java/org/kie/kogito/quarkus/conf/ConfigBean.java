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

import javax.inject.Singleton;

import org.eclipse.microprofile.config.ConfigProvider;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.conf.StaticConfigBean;

import io.quarkus.runtime.annotations.Recorder;

@Singleton
@Recorder
public class ConfigBean extends StaticConfigBean {

    private static KogitoGAV gav;

    public static void setRuntimeGav(String groupId, String artifactId, String version) {
        gav = new KogitoGAV(groupId, artifactId, version);
    }

    @Override
    public String getServiceUrl() {
        return ConfigProvider.getConfig().getOptionalValue("kogito.service.url", String.class).orElse("");
    }

    @Override
    public Optional<KogitoGAV> getGav() {
        return Optional.ofNullable(gav);
    }

    @Override
    public boolean failOnEmptyBean() {
        return ConfigProvider.getConfig().getOptionalValue("kogito.jackson.fail-on-empty-bean", Boolean.class).orElse(false);
    }

    @Override
    public boolean useCloudEvents() {
        return ConfigProvider.getConfig().getOptionalValue("kogito.messaging.as-cloudevents", Boolean.class).orElse(true);
    }
}
