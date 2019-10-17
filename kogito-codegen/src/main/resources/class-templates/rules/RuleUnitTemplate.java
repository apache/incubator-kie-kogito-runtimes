package $Package$;

import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

public class $Name$ extends org.kie.kogito.rules.impl.AbstractRuleUnit<$ModelName$> {

    private final KieBase kbase;

    public $Name$() {
        this(new $Application$());
    }

    public $Name$(org.kie.kogito.Application app) {
        super(app);
        this.kbase = createKieBase();
    }

    public $InstanceName$ createInstance($ModelName$ value) {
        return new $InstanceName$(
                this,
                value,
                createLegacySession());
    }

    private KieBase createKieBase() {
        return app.ruleUnits().ruleRuntimeBuilder().getKieBase( $ModelClass$ );
    }

    private org.kie.api.runtime.KieSession createLegacySession() {
        return kbase.newKieSession();
    }
}
