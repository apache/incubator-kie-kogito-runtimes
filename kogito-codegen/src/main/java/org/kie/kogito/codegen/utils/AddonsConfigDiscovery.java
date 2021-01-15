/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.codegen.utils;

import org.kie.kogito.codegen.AddonsConfig;
import org.kie.kogito.codegen.context.KogitoBuildContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * Utility class that performs automatic addons discovery
 */
public class AddonsConfigDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddonsConfigDiscovery.class);

    public static final String persistenceFactoryClass = "org.kie.kogito.persistence.KogitoProcessInstancesFactory";
    public static final String prometheusClass = "org.kie.kogito.monitoring.prometheus.common.rest.MetricsResource";
    public static final String monitoringCoreClass = "org.kie.kogito.monitoring.core.common.MonitoringRegistry";
    public static final String tracingClass = "org.kie.kogito.tracing.decision.DecisionTracingListener";
    public static final String knativeEventingClass = "org.kie.kogito.events.knative.ce.extensions.KogitoProcessExtension";
    public static final String quarkusCloudEvents = "org.kie.kogito.addon.cloudevents.quarkus.QuarkusCloudEventEmitter";
    public static final String springCloudEvents = "org.kie.kogito.addon.cloudevents.spring.SpringKafkaCloudEventEmitter";

    private AddonsConfigDiscovery() {
        // utility class
    }

    public static AddonsConfig discover(KogitoBuildContext context) {
        return discover(context::hasClassAvailable);
    }

    public static AddonsConfig discover(Predicate<String> classAvailabilityResolver) {
        boolean usePersistence = classAvailabilityResolver.test(persistenceFactoryClass);
        boolean usePrometheusMonitoring = classAvailabilityResolver.test(prometheusClass);
        boolean useMonitoring = usePrometheusMonitoring || classAvailabilityResolver.test(monitoringCoreClass);
        boolean useTracing = classAvailabilityResolver.test(tracingClass);
        boolean useKnativeEventing = classAvailabilityResolver.test(knativeEventingClass);
        boolean useCloudEvents = classAvailabilityResolver.test(quarkusCloudEvents) || classAvailabilityResolver.test(springCloudEvents);

        AddonsConfig addonsConfig = AddonsConfig.builder()
                .withPersistence(usePersistence)
                .withMonitoring(useMonitoring)
                .withPrometheusMonitoring(usePrometheusMonitoring)
                .withTracing(useTracing)
                .withKnativeEventing(useKnativeEventing)
                .withCloudEvents(useCloudEvents)
                .build();

        LOGGER.info("Performed addonsConfig discovery, found: {}", addonsConfig);

        return addonsConfig;
    }
}
