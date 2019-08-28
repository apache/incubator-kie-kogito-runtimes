package $Package$;

import org.kie.api.runtime.KieSession;
import org.kie.kogito.rules.RuleEventListenerConfig;

public class $Name$ extends org.kie.kogito.rules.impl.AbstractRuleUnit<$ModelName$> {


    public $Name$(org.kie.kogito.Application app) {
        super(app);
    }

    public $InstanceName$ createInstance($ModelName$ value) {
        return new $InstanceName$(
                this,
                value,
                createLegacySession());
    }

    private KieSession createLegacySession() {
        org.drools.core.impl.InternalKnowledgeBase kb =
                org.drools.modelcompiler.builder.KieBaseBuilder.createKieBaseFromModel(
                        new $RuleModelName$());
        KieSession ks = kb.newKieSession();
        RuleEventListenerConfig ruleEventListenerConfig = app.config().rule().ruleEventListeners();
        ruleEventListenerConfig.agendaListeners().forEach(ks::addEventListener);
        ruleEventListenerConfig.ruleRuntimeListeners().forEach(ks::addEventListener);
        return ks;
    }
}
