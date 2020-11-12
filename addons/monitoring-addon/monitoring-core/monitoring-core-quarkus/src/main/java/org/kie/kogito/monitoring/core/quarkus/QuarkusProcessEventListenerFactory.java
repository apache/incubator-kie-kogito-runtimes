package org.kie.kogito.monitoring.core.quarkus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.kie.internal.event.rule.RuleEventListener;
import org.kie.kogito.monitoring.core.api.process.ProcessEventListener;

@ApplicationScoped
public class QuarkusProcessEventListenerFactory {

    @Inject
    @Any
    Instance<ProcessEventListener> beans;

    @Inject
    @Any
    Instance<RuleEventListener> ruleEventListeners;

    @Produces
    public ProcessEventListener getit() {
        if (beans.stream().count() == 1){
            System.out.println("Only one listener.");
            return beans.get();
        }
        if (beans.stream().count() >= 2){
            System.out.println("Too many monitoring listeners!");
        }
        System.out.println("Choosing listener");
        return beans.stream().filter(x -> !x.getClass().getSimpleName().equals("QuarkusDefaultProcessEventListenerConfig")).findFirst().get();
    }

    @Produces
    public RuleEventListener getsuca(){
            if (ruleEventListeners.stream().count() == 1){
                System.out.println("Only one rule listener.");
                return ruleEventListeners.get();
            }
            if (ruleEventListeners.stream().count() >= 2){
                System.out.println("Too many rule monitoring listeners!");
            }
            System.out.println("Choosing rule listener");
            return ruleEventListeners.stream().filter(x -> !x.getClass().getSimpleName().equals("QuarkusMonitoringDefaultRuleEventListenerConfig")).findFirst().get();
        }
}
