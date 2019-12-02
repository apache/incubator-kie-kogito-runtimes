package org.drools.project.model;

import org.kie.kogito.rules.KieRuntimeBuilder;
import org.kie.kogito.rules.units.impl.SessionData;
import org.kie.kogito.rules.units.impl.SessionRuleUnitInstance;
import org.kie.kogito.rules.units.impl.SessionUnit;

public class SessionRuleUnit extends SessionUnit {
    
    KieRuntimeBuilder runtimeBuilder;

    @Override
    public SessionRuleUnitInstance createInstance(SessionData memory, String name ) {
        return new SessionRuleUnitInstance(this, memory, runtimeBuilder.newKieSession("$SessionName$"));
    }
}