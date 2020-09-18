/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package $Package$;

import org.kie.kogito.Config;
import org.kie.kogito.StaticApplication;
import org.kie.kogito.process.Processes;

@org.springframework.stereotype.Component
@org.springframework.web.context.annotation.ApplicationScope
public class Application extends StaticApplication {

    @org.springframework.beans.factory.annotation.Autowired()
    public Application(
            Config config,
            java.util.Collection<Processes> processes/*,
            java.util.Collection<RuleUnits> ruleUnits,
            java.util.Collection<DecisionModels> decisionModels,
            java.util.Collection<PredictionModels> predictionModels,
            */) {
        this.config = config;
        this.processes = orNull(processes);
        this.ruleUnits = null /* $RuleUnits$ */;
        this.decisionModels = null /* $DecisionModels$ */;
        this.predictionModels = null /* $PredictionModels$ */;

        if (config().process() != null) {
            unitOfWorkManager().eventManager().setAddons(config().addons());
        }
    }

    private static <T> T orNull(java.util.Collection<T> collection) {
        if (collection.isEmpty()) {
            return null;
        } else {
            if (collection.size() > 1) {
                throw new IllegalArgumentException("Found too many injection candidates " + collection);
            }
            return collection.iterator().next();
        }
    }
}
