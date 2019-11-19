/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.workflow.instance.node;

import java.util.Date;

import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.runtime.process.EventListener;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.kogito.jobs.ExpirationTime;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.jobs.ProcessInstanceJobDescription;
import org.kie.services.time.TimerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerNodeInstance extends StateBasedNodeInstance implements EventListener {

    private static final long serialVersionUID = 510l;
    private static final Logger logger = LoggerFactory.getLogger(TimerNodeInstance.class);
    
    private String timerId;
    private boolean oneTimeTimer;
    
    public TimerNode getTimerNode() {
        return (TimerNode) getNode();
    }
    
    public String getTimerId() {
    	return timerId;
    }
    
    public void internalSetTimerId(String timerId) {
    	this.timerId = timerId;
    }

    @Override
    public void internalTrigger(NodeInstance from, String type) {
        if (!org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
            throw new IllegalArgumentException(
                "A TimerNode only accepts default incoming connections!");
        }
        triggerTime = new Date();        
        ExpirationTime expirationTime = createTimerInstance(getTimerNode().getTimer());
        if (getTimerInstances() == null) {
        	addTimerListener();
        }
        JobsService jobService = ((InternalProcessRuntime)
                getProcessInstance().getKnowledgeRuntime().getProcessRuntime()).getJobsService();
        timerId = jobService.scheduleProcessInstanceJob(ProcessInstanceJobDescription.of(getTimerNode().getTimer().getId(), expirationTime, getProcessInstance().getId(), getProcessInstance().getRootProcessInstanceId(), getProcessInstance().getProcessId(), getProcessInstance().getRootProcessId()));
        
        oneTimeTimer = expirationTime.repeatInterval() == null;
    }

    
    public void signalEvent(String type, Object event) {
    	if ("timerTriggered".equals(type)) {
    		TimerInstance timer = (TimerInstance) event;
            if (timer.getId().equals(timerId)) {
                triggerCompleted(oneTimeTimer || timer.getRepeatLimit() == 0);
            }
    	}
    }
    
    public String[] getEventTypes() {
    	return new String[] { "timerTriggered" };
    }
    
    public void triggerCompleted(boolean remove) {
        triggerCompleted(org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE, remove);
    }
    
    @Override
    public void cancel() {
        ((InternalProcessRuntime)
                getProcessInstance().getKnowledgeRuntime().getProcessRuntime()).getJobsService().cancelJob(timerId);
        super.cancel();
    }
    
    public void addEventListeners() {
        super.addEventListeners();
        if (getTimerInstances() == null) {
        	addTimerListener();
        }
    }
    
    public void removeEventListeners() {
        super.removeEventListeners();
        ((WorkflowProcessInstance) getProcessInstance()).removeEventListener("timerTriggered", this, false);
    }

}
