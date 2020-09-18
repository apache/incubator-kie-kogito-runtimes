/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import org.kie.api.runtime.KieSession;
import org.kie.kogito.rules.RuleEventListenerConfig;
import org.kie.kogito.rules.units.impl.AbstractRuleUnit;

public class $Name$ extends AbstractRuleUnit<$ModelName$> {

    public $Name$(org.kie.kogito.Application app) {
        super(app);
    }

    public $InstanceName$ internalCreateInstance($ModelName$ value) {
        return new $InstanceName$( this, value, createLegacySession());
    }

    private KieSession createLegacySession() {
        KieSession ks = app.ruleUnits().ruleRuntimeBuilder().newKieSession( $ModelClass$ );
        ((org.drools.core.impl.KogitoStatefulKnowledgeSessionImpl)ks).setApplication( app );
        if (app.config() != null && app.config().rule() != null) {
            RuleEventListenerConfig ruleEventListenerConfig = app.config().rule().ruleEventListeners();
            ruleEventListenerConfig.agendaListeners().forEach(ks::addEventListener);
            ruleEventListenerConfig.ruleRuntimeListeners().forEach(ks::addEventListener);
        }
        return ks;
    }
}
