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

import org.kie.kogito.KogitoGAV;
import org.kie.kogito.conf.StaticConfigBean;

@Singleton
public class ConfigBean extends StaticConfigBean {

    private ConfigBeanRecorder configBeanRecorder;

    public ConfigBean(ConfigBeanRecorder configBeanRecorder) {
        super();
        this.configBeanRecorder = configBeanRecorder;
    }

    @Override
    public String getServiceUrl() {
        return configBeanRecorder.getServiceUrl();
    }

    @Override
    public Optional<KogitoGAV> getGav() {
        return Optional.ofNullable(configBeanRecorder.getGav());
    }

    @Override
    public boolean failOnEmptyBean() {
        return configBeanRecorder.isFailOnEmptyBean();
    }

    @Override
    public boolean useCloudEvents() {
        return configBeanRecorder.isUseCloudEvents();
    }
}
