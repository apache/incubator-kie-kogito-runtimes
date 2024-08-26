/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.usertask.impl.model;

import org.kie.kogito.process.workitems.impl.DefaultWorkItemLifeCycle;

/**
 * Base life cycle definition for human tasks. It comes with following phases
 * 
 * <ul>
 * <li>Active</li>
 * <li>Claim</li>
 * <li>Release</li>
 * <li>Complete</li>
 * <li>Skip</li>
 * <li>Abort</li>
 * </ul>
 * At the beginning human task enters
 *
 * <pre>
 * Active
 * </pre>
 *
 * phase. From there it can go to
 * 
 * <ul>
 * <li>Claim</li>
 * <li>Complete</li>
 * <li>Skip</li>
 * <li>Abort</li>
 * </ul>
 * 
 * at any time. At each phase data can be associated and by that set on work item.
 */
public class DefaultHumanTaskLifeCycle extends DefaultWorkItemLifeCycle {

    //    private static final Logger logger = LoggerFactory.getLogger(DefaultHumanTaskLifeCycle.class);
    //
    //    private Map<String, WorkItemLifeCyclePhase> phases = new LinkedHashMap<>();
    //
    //    public DefaultHumanTaskLifeCycle() {
    //        phases.put(Claim.ID, new Claim());
    //        phases.put(Release.ID, new Release());
    //        phases.put(Complete.ID, new Complete());
    //        phases.put(Skip.ID, new Skip());
    //        phases.put(Active.ID, new Active());
    //        phases.put(Abort.ID, new Abort());
    //    }
    //
    //    @Override
    //    public WorkItemLifeCyclePhase phaseById(String phaseId) {
    //        return phases.get(phaseId);
    //    }
    //
    //    @Override
    //    public Collection<WorkItemLifeCyclePhase> phases() {
    //        return phases.values();
    //    }
    //
    //    @Override
    //    public Map<String, Object> transitionTo(KogitoWorkItem workItem, KogitoWorkItemManager manager, WorkItemTransition<Map<String, Object>> transition) {
    //        logger.debug("Transition method invoked for work item {} to transition to {}, currently in phase {} and status {}", workItem.getStringId(), transition.phase(), workItem.getPhaseId(),
    //                workItem.getPhaseStatus());
    //        InternalHumanTaskWorkItem humanTaskWorkItem = (InternalHumanTaskWorkItem) workItem;
    //
    //        WorkItemLifeCyclePhase targetPhase = phases.get(transition.phase());
    //        if (targetPhase == null) {
    //            logger.debug("Target life cycle phase '{}' does not exist in {}", transition.phase(), this.getClass().getSimpleName());
    //            throw new InvalidLifeCyclePhaseException(transition.phase());
    //        }
    //
    //        WorkItemLifeCyclePhase currentPhase = phases.get(humanTaskWorkItem.getPhaseId());
    //
    //        if (!targetPhase.canTransition(currentPhase)) {
    //            logger.debug("Target life cycle phase '{}' cannot transition from current state '{}'", targetPhase.id(), currentPhase.id());
    //            throw new InvalidTransitionException("Cannot transition from " + humanTaskWorkItem.getPhaseId() + " to " + targetPhase.id());
    //        }
    //
    //        if (!targetPhase.id().equals(Active.ID) && !targetPhase.id().equals(Abort.ID) && !humanTaskWorkItem.enforce(transition.policies().toArray(new Policy[transition.policies().size()]))) {
    //            throw new NotAuthorizedException("User is not authorized to access task instance with id " + humanTaskWorkItem.getStringId());
    //        }
    //
    //        humanTaskWorkItem.setPhaseId(targetPhase.id());
    //        humanTaskWorkItem.setPhaseStatus(targetPhase.status());
    //
    //        targetPhase.apply(humanTaskWorkItem, transition);
    //        if (transition.data() != null) {
    //            logger.debug("Updating data for phase {} and work item {}", targetPhase.id(), humanTaskWorkItem.getStringId());
    //            humanTaskWorkItem.setResults(transition.data());
    //        }
    //        logger.debug("Transition for work item {} to {} done, currently in phase {} and status {}", workItem.getStringId(), transition.phase(), workItem.getPhaseId(), workItem.getPhaseStatus());
    //
    //        if (targetPhase.isTerminating()) {
    //            logger.debug("Target life cycle phase '{}' is terminiating, completing work item {}", targetPhase.id(), humanTaskWorkItem.getStringId());
    //            // since target life cycle phase is terminating completing work item
    //            ((InternalKogitoWorkItemManager) manager).internalCompleteWorkItem(humanTaskWorkItem);
    //        }
    //
    //        return data(humanTaskWorkItem);
    //    }

}
