package $Package$;

import org.kie.api.runtime.KieSession;
import org.kie.kogito.rules.units.impl.AbstractRuleUnit;

public class $Name$ extends AbstractRuleUnit<$ModelName$> {

    public $Name$() {
        this(new $Application$());
    }

    public $Name$(org.kie.kogito.Application app) {
        super(app);
    }

    public $InstanceName$ internalCreateInstance($ModelName$ value) {
        return new $InstanceName$( this, value, createLegacySession());
    }

    private KieSession createLegacySession() {
        $Package$.Application.RuleUnits ruleUnits = ($Package$.Application.RuleUnits) app.ruleUnits();
        KieSession ks = ruleUnits.ruleRuntimeBuilder().newKieSession( $ModelClass$ );
        ((org.drools.core.impl.StatefulKnowledgeSessionImpl)ks).setApplication( app );
        if (app.config() != null && app.config().rule() != null) {
            org.kie.kogito.rules.RuleEventListenerConfig ruleEventListenerConfig = app.config().rule().ruleEventListeners();
            if (ruleEventListenerConfig.agendaListener() != null) {
                ks.addEventListener(new org.kie.kogito.internal.rules.AgendaEventListenerAdapter(ruleEventListenerConfig.agendaListener()));
            }
            if (ruleEventListenerConfig.dataSourceListener() != null) {
                ks.addEventListener(new org.kie.kogito.internal.rules.RuleRuntimeEventListenerAdapter(ruleEventListenerConfig.dataSourceListener()));
            }
        }
        return ks;
    }
}
