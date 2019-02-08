package org.jbpm.process.instance;

import org.drools.core.time.TimerService;
import org.drools.core.time.impl.JDKTimerService;
import org.jbpm.process.instance.event.LightSignalManager;
import org.jbpm.process.instance.event.SignalManager;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManager;

public class LightProcessRuntimeServiceProvider implements ProcessRuntimeServiceProvider {

    private final TimerService timerService = new JDKTimerService();
    private final ProcessInstanceManager processInstanceManager = new DefaultProcessInstanceManager();
    private final SignalManager signalManager = new LightSignalManager(processInstanceManager);

    @Override
    public TimerService getTimerService() {
        return timerService;
    }

    @Override
    public ProcessInstanceManager getProcessInstanceManager() {
        return processInstanceManager;
    }

    @Override
    public SignalManager getSignalManager() {
        return signalManager;
    }
}
