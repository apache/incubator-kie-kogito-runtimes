/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kie.kogito.monitoring.elastic.quarkus;

import java.util.Optional;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import io.quarkus.arc.config.ConfigProperties;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.monitoring.elastic.common.ElasticConfigFactory;
import org.kie.kogito.monitoring.elastic.common.ElasticRegistry;
import org.kie.kogito.monitoring.elastic.common.KogitoElasticConfig;

@Singleton
@Startup
@ConfigProperties(prefix = "kogito.addon.monitoring.elastic")
public class QuarkusElasticRegistryProvider extends ElasticRegistry {

    @ConfigProperty(name = "host")
    public Optional<String> elasticHost;
    @ConfigProperty(name = "index")
    public Optional<String> index;
    @ConfigProperty(name = "step")
    public Optional<String> step;
    @ConfigProperty(name = "indexDateFormat")
    public Optional<String> indexDateFormat;
    @ConfigProperty(name = "timestampFieldName")
    public Optional<String> timestampFieldName;
    @ConfigProperty(name = "autoCreateIndex")
    public Optional<String> autoCreateIndex;
    @ConfigProperty(name = "userName")
    public Optional<String> userName;
    @ConfigProperty(name = "password")
    public Optional<String> password;
    @ConfigProperty(name = "pipeline")
    public Optional<String> pipeline;
    @ConfigProperty(name = "indexDateSeparator")
    public Optional<String> indexDateSeparator;
    @ConfigProperty(name = "documentType")
    public Optional<String> documentType;

    public void config(@Observes StartupEvent event) {
        ElasticConfigFactory elasticConfigFactory = new ElasticConfigFactory();
        elasticHost.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.HOST_KEY, x));
        index.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.INDEX_KEY, x));
        step.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.STEP_KEY, x));
        indexDateFormat.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.INDEX_DATE_FORMAT_KEY, x));
        timestampFieldName.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.TIMESTAMP_FIELD_NAME_KEY, x));
        autoCreateIndex.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.AUTO_CREATE_INDEX_KEY, x));
        userName.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.USERNAME_KEY, x));
        password.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.PASSWORD_KEY, x));
        pipeline.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.PIPELINE_KEY, x));
        indexDateSeparator.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.INDEX_DATE_SEPARATOR_KEY, x));
        documentType.ifPresent(x -> elasticConfigFactory.setProperty(KogitoElasticConfig.DOCUMENT_TYPE_KEY, x));
        super.start(elasticConfigFactory.getElasticConfig());
    }
}