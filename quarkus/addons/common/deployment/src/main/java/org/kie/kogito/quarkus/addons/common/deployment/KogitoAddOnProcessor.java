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
package org.kie.kogito.quarkus.addons.common.deployment;

import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildStep;

import static java.util.Arrays.asList;

/**
 * Abstract class for Add-Ons processors that requires a set of capabilities to be presented.
 * <p/>
 * When extending this base class, if your add-on requires a particular set of capabilities,
 * inform them in the constructor. For example:
 * <p/>
 * 
 * <pre>
 * public MyKogitoAddonProcessor() {
 *     super(KogitoCapability.RULES);
 * }
 * </pre>
 *
 * You don't need to point to a particular engine if your add-on fits any set of it.
 * Just by extending this class, it will make any of {@link KogitoCapability#ENGINES} to be presented
 * in the user's classpath.
 * 
 * @see <a href="https://quarkus.io/guides/capabilities">Quarkus Extension Capabilities</a>
 */
public abstract class KogitoAddOnProcessor {

    private final List<KogitoCapability> requiredCapabilities;

    /**
     * Required capabilities that this Add-On depends on
     *
     * @see <a href="https://quarkus.io/guides/capabilities#declaring-capabilities">Declaring Capabilities</a>
     */
    public KogitoAddOnProcessor(final KogitoCapability... requiredCapabilities) {
        this.requiredCapabilities = asList(requiredCapabilities);
    }

    /**
     * {@link BuildStep} to verify if all {@link Capabilities} are presented in the current classpath.
     *
     */
    @BuildStep
    void verifyCapabilities(final Capabilities capabilities) {
        if (this.requiredCapabilities.isEmpty()) {
            if (KogitoCapability.ENGINES.stream().noneMatch(kc -> capabilities.isPresent(kc.getCapability()))) {
                throw this.exceptionForEngineNotPresent();
            }
        } else {
            final List<KogitoCapability> missing = requiredCapabilities.stream()
                    .filter(kc -> capabilities.isMissing(kc.getCapability()))
                    .collect(Collectors.toList());
            if (!missing.isEmpty()) {
                throw this.exceptionForRequiredCapabilities(missing);
            }
        }
    }

    private IllegalStateException exceptionForRequiredCapabilities(List<KogitoCapability> missingCapabilities) {
        final StringBuilder sb = new StringBuilder();
        sb.append("The following capabilities are missing: \n");
        missingCapabilities.forEach(c -> {
            sb.append("\t - ").append(c.getCapability()).append("\n");
            sb.append("\t\t offered by the artifact ")
                    .append(KogitoCapability.KOGITO_GROUP_ID)
                    .append(":")
                    .append(c.getOfferedBy())
                    .append("\n");
        });
        sb.append("Add the above artifacts in your project's pom.xml file");
        return new IllegalStateException(sb.toString());
    }

    private IllegalStateException exceptionForEngineNotPresent() {
        final StringBuilder sb = new StringBuilder();
        sb.append("This Kogito Quarkus Add-on requires at least one of the following Kogito Extensions: \n");
        KogitoCapability.ENGINES.forEach(c -> {
            sb.append("\t - ").append(c.getCapability()).append("\n");
            sb.append("\t\t offered by the artifact ")
                    .append(KogitoCapability.KOGITO_GROUP_ID)
                    .append(":")
                    .append(c.getOfferedBy())
                    .append("\n");
        });
        sb.append("Add one of the above artifacts in your project's pom.xml file");
        return new IllegalStateException(sb.toString());
    }

}
