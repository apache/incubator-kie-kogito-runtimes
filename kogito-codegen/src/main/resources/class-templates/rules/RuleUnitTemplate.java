package $Package$;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.UnitRuntime;
import org.kie.kogito.rules.RuleEventListenerConfig;
import org.kie.kogito.rules.units.impl.AbstractRuleUnit;

public class $Name$ extends AbstractRuleUnit<$ModelName$> {

    public $Name$() {
        this(new $Application$());
    }

    public $Name$(org.kie.kogito.Application app) {
        super(app);
    }

    public $InstanceName$ internalCreateInstance($ModelName$ value) {
        return new $InstanceName$( this, value, createUnitRuntime());
    }

    private KieSession createLegacySession() {
        KieSession ks = app.ruleUnits().ruleRuntimeBuilder().newKieSession( $ModelClass$ );
        ((org.drools.core.impl.StatefulKnowledgeSessionImpl)ks).setApplication( app );
        if (app.config() != null && app.config().rule() != null) {
            RuleEventListenerConfig ruleEventListenerConfig = app.config().rule().ruleEventListeners();
            ruleEventListenerConfig.agendaListeners().forEach(ks::addEventListener);
            ruleEventListenerConfig.ruleRuntimeListeners().forEach(ks::addEventListener);
        }
        return ks;
    }

    private UnitRuntime createUnitRuntime() {
        UnitRuntime runtime = app.unitRuntime( $ModelClass$ );
        org.drools.core.common.InternalWorkingMemory wm = (org.drools.core.common.InternalWorkingMemory) runtime;
        if (app.config() != null && app.config().rule() != null) {
            RuleEventListenerConfig ruleEventListenerConfig = app.config().rule().ruleEventListeners();
            ruleEventListenerConfig.agendaListeners().forEach(wm::addEventListener);
            ruleEventListenerConfig.ruleRuntimeListeners().forEach(wm::addEventListener);
        }
        return runtime;
    }
}
