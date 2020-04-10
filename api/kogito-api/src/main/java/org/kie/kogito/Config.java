/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito;

import org.kie.kogito.decision.DecisionConfig;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.rules.RuleConfig;

/**
 * Provides general configuration of Kogito application
 */
public interface Config {

    /**
     * Provides process specific configuration
     *
     * @return process specific configuration or null of no process is found in the application
     */
    ProcessConfig process();

    /**
     * Provides rule specific configuration
     *
     * @return rule specific configuration or null of no rule is found in the application
     */
    RuleConfig rule();

    /**
     * Provides decision specific configuration
     *
     * @return decision specific configuration or null of no decision is found in the application
     */
    DecisionConfig decision();

    /**
     * Provides access to addons in the application.
     *
     * @return addons available in the application
     */
    default Addons addons() {
        return Addons.EMTPY;
    }

}
