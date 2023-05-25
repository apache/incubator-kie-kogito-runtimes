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
package org.kie.kogito.process;

import org.kie.kogito.config.ConfigBean;
import org.kie.kogito.correlation.CorrelationService;
import org.kie.kogito.event.correlation.DefaultCorrelationService;
import org.kie.kogito.process.version.ProjectVersionProcessVersionResolver;
import org.kogito.workitem.rest.RestWorkItemHandlerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;

@Configuration
public class KogitoBeanProducer {

    ConfigBean configBean;

    @Autowired
    public KogitoBeanProducer(ConfigBean configBean) {
        this.configBean = configBean;
    }

    @Bean
    CorrelationService correlationService() {
        return new DefaultCorrelationService();
    }

    @Bean
    @ConditionalOnProperty(value = "kogito.workflow.version-strategy", havingValue = "project")
    ProcessVersionResolver projectVersionResolver() {
        return new ProjectVersionProcessVersionResolver(configBean.getGav().orElseThrow(() -> new RuntimeException("Unable to use kogito.workflow.version-strategy without a project GAV")));
    }

    @Bean
    @ConditionalOnMissingBean(WebClientOptions.class)
    WebClientOptions sslDefaultOptions() {
        return RestWorkItemHandlerUtils.sslWebClientOptions();
    }

    @Bean
    @ConditionalOnMissingBean(WebClientOptions.class)
    Vertx vertx() {
        return Vertx.vertx();
    }
}
