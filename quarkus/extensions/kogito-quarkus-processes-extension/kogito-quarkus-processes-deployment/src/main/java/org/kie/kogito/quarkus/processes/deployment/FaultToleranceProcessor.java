/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.kie.kogito.quarkus.processes.deployment;

import java.util.function.BooleanSupplier;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.SystemPropertyBuildItem;

import jakarta.interceptor.Interceptor;

public class FaultToleranceProcessor {

    private static final String SMALLRYE_FAULT_TOLERANCE_CAPABILITY = "io.quarkus.smallrye.faulttolerance";

    public static class IsFaultToleranceEnabled implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            return checkFaultToleranceProperty("kogito.faultToleranceEnabled") ||
                    checkFaultToleranceProperty("kogito.processes.faultToleranceEnabled") ||
                    checkFaultToleranceProperty("kogito.usertasks.faultToleranceEnabled");
        }

        private boolean checkFaultToleranceProperty(String propertyName) {
            return ConfigProvider.getConfig().getOptionalValue(propertyName, Boolean.class).orElse(true);
        }
    }

    @BuildStep(onlyIf = IsFaultToleranceEnabled.class)
    public void setupKogitoFaultTolerance(
            BuildProducer<SystemPropertyBuildItem> systemProperties,
            Capabilities capabilities) {

        if (capabilities.isPresent(SMALLRYE_FAULT_TOLERANCE_CAPABILITY)) {
            systemProperties.produce(new SystemPropertyBuildItem("mp.fault.tolerance.interceptor.priority", String.valueOf(Interceptor.Priority.PLATFORM_BEFORE + 100)));
        }
    }
}
