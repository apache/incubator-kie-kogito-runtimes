/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addons.quarkus.monitoring.prometheus.deprecated;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;

/**
 * @deprecated The library monitoring-prometheus-quarkus-addon is deprecated!!!!! Please use kogito-addons-quarkus-monitoring-prometheus instead
 */
@Startup
@ApplicationScoped
@Deprecated
public class DeprecatedQuarkusMonitoringPrometheus {

    public static final Logger LOGGER = LoggerFactory.getLogger(DeprecatedQuarkusMonitoringPrometheus.class);

    public DeprecatedQuarkusMonitoringPrometheus() {
        LOGGER.warn("The library monitoring-prometheus-quarkus-addon is deprecated!!!!! Please use kogito-addons-quarkus-monitoring-prometheus instead");
    }

}
