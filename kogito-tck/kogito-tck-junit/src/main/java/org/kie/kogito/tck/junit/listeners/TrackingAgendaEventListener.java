/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.tck.junit.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;

public class TrackingAgendaEventListener extends DefaultAgendaEventListener {

    private Map<String, Integer> rulesFired;

    
    public TrackingAgendaEventListener() {
        rulesFired = new HashMap<String, Integer>();
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        String rule = event.getMatch().getRule().getName();
        if (isRuleFired(rule)) {
            rulesFired.put(rule, rulesFired.get(rule) + 1);
        } else {
            rulesFired.put(rule, 1);
        }
    }

    /**
     * Return true if the rule was fired at least once
     *
     * @param rule - name of the rule
     * @return true if the rule was fired
     */
    public boolean isRuleFired(String rule) {
        return rulesFired.containsKey(rule);
    }

    /**
     * Returns number saying how many times the rule was fired
     *
     * @param rule - name ot the rule
     * @return number how many times rule was fired, 0 if rule wasn't fired
     */
    public int ruleFiredCount(String rule) {
        if (isRuleFired(rule)) {
            return rulesFired.get(rule);
        } else {
            return 0;
        }
    }

    /**
     * @return how many rules were fired
     */
    public int rulesCount() {
        return rulesFired.size();
    }

    public Collection<String> getFiredRules() {
        return rulesFired.keySet();
    }

}
