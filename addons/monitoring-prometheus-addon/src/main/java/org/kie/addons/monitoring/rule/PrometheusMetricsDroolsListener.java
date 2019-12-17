/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.addons.monitoring.rule;

import java.util.HashMap;

import org.drools.core.event.rule.impl.AfterActivationFiredEventImpl;
import org.drools.core.event.rule.impl.BeforeActivationFiredEventImpl;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.kogito.rules.Match;
import org.kie.kogito.rules.listeners.AgendaListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.addons.monitoring.rule.PrometheusMetrics.getDroolsEvaluationTimeHistogram;

public class PrometheusMetricsDroolsListener implements AgendaListener {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsDroolsListener.class);
    private final String identifier;

    final HashMap<Match, Long> map = new HashMap<>();

    public PrometheusMetricsDroolsListener(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void beforeMatchFired(Match match) {
        long nanoTime = System.nanoTime();
        map.put(match, nanoTime);
    }

    @Override
    public void afterMatchFired(Match match) {
        long startTime = map.getOrDefault(match, 0l);
        long elapsed = System.nanoTime() - startTime;
        String ruleName = match.getRule().getName();

        getDroolsEvaluationTimeHistogram()
                .labels(identifier, ruleName)
                .observe(elapsed);
        if (logger.isDebugEnabled()) {
            logger.debug("Elapsed time: " + elapsed);
        }
    }


    public BeforeActivationFiredEventImpl getBeforeImpl(BeforeMatchFiredEvent e) {
        return (BeforeActivationFiredEventImpl)e;
    }

    public AfterActivationFiredEventImpl getAfterImpl(AfterMatchFiredEvent e) {
        return (AfterActivationFiredEventImpl)e;
    }
}
