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
package org.kie.kogito.prediction.api;

import org.kie.kogito.usertask.impl.lifecycle.DefaultUserTaskLifeCycle;

public class PredictionAwareHumanTaskLifeCycle extends DefaultUserTaskLifeCycle {

    //    private static final Logger logger = LoggerFactory.getLogger(PredictionAwareHumanTaskLifeCycle.class);
    //
    //    private PredictionService predictionService;
    //
    //    public PredictionAwareHumanTaskLifeCycle(PredictionService predictionService) {
    //        this.predictionService = Objects.requireNonNull(predictionService);
    //    }
    //
    //    @Override
    //    public Map<String, Object> transitionTo(KogitoWorkItem workItem, KogitoWorkItemManager manager, Transition<Map<String, Object>> transition) {
    //        LifeCyclePhase targetPhase = phaseById(transition.phase());
    //        if (targetPhase == null) {
    //            logger.debug("Target life cycle phase '{}' does not exist in {}", transition.phase(), this.getClass().getSimpleName());
    //            throw new InvalidLifeCyclePhaseException(transition.phase());
    //        }
    //
    //        InternalHumanTaskWorkItem humanTaskWorkItem = (InternalHumanTaskWorkItem) workItem;
    //        if (targetPhase.id().equals(Active.ID)) {
    //
    //            PredictionOutcome outcome = predictionService.predict(workItem, workItem.getParameters());
    //            logger.debug("Prediction service returned confidence level {} for work item {}", outcome.getConfidenceLevel(), humanTaskWorkItem.getStringId());
    //
    //            if (outcome.isCertain()) {
    //                humanTaskWorkItem.setResults(outcome.getData());
    //                logger.debug("Prediction service is certain (confidence level {}) on the outputs, completing work item {}", outcome.getConfidenceLevel(), humanTaskWorkItem.getStringId());
    //                ((InternalKogitoWorkItemManager) manager).internalCompleteWorkItem(humanTaskWorkItem);
    //
    //                return outcome.getData();
    //            } else if (outcome.isPresent()) {
    //                logger.debug("Prediction service is NOT certain (confidence level {}) on the outputs, setting recommended outputs on work item {}", outcome.getConfidenceLevel(),
    //                        humanTaskWorkItem.getStringId());
    //                humanTaskWorkItem.setResults(outcome.getData());
    //
    //            }
    //        }
    //
    //        // prediction service does work only on activating tasks
    //        Map<String, Object> data = super.transitionTo(workItem, manager, transition);
    //        if (targetPhase.id().equals(Complete.ID)) {
    //            // upon actual transition train the data if it's completion phase
    //            predictionService.train(humanTaskWorkItem, workItem.getParameters(), data);
    //        }
    //        return data;
    //    }

}
