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
package org.kie.kogito.quarkus.addons.common.deployment;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Data Holder to use with {@link RequireCapabilityKogitoAddOnProcessor} to describe the Kogito Capability required by a given add-on.
 */
public final class KogitoCapability {

    public static final KogitoCapability DECISIONS = new KogitoCapability("org.drools.decisions", new KogitoCapabilityExtension("org.drools", "drools-quarkus-decisions"));
    public static final KogitoCapability PROCESSES = new KogitoCapability("org.jbpm.processes", new KogitoCapabilityExtension("org.jbpm", "jbpm-quarkus"));
    public static final KogitoCapability PREDICTIONS = new KogitoCapability("org.kie.predictions", new KogitoCapabilityExtension("org.kie", "kie-quarkus-predictions"));
    public static final KogitoCapability RULES = new KogitoCapability("org.drools.rules", new KogitoCapabilityExtension("org.drools", "drools-quarkus-rules"));
    public static final KogitoCapability SERVERLESS_WORKFLOW =
            new KogitoCapability("org.apache.kie.sonataflow.serverless-workflow", new KogitoCapabilityExtension("org.apache.kie.sonataflow", "sonataflow-quarkus"));

    public static final List<KogitoCapability> ENGINES = asList(
            KogitoCapability.DECISIONS,
            KogitoCapability.PROCESSES,
            KogitoCapability.PREDICTIONS,
            KogitoCapability.RULES,
            KogitoCapability.SERVERLESS_WORKFLOW);

    private final String capability;
    private final KogitoCapabilityExtension offeredBy;

    public KogitoCapability(final String capability, final KogitoCapabilityExtension offeredBy) {
        this.capability = capability;
        this.offeredBy = offeredBy;
    }

    public String getCapability() {
        return capability;
    }

    public KogitoCapabilityExtension getOfferedBy() {
        return offeredBy;
    }
}
