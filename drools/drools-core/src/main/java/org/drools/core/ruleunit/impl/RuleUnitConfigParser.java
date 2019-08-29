package org.drools.core.ruleunit.impl;

import org.drools.core.ClockType;
import org.kie.api.conf.EventProcessingOption;
import org.kie.kogito.rules.RuleConfig;

public class RuleUnitConfigParser {
    public static EventProcessingOption parseEventProcessing(RuleConfig.EventProcessing mode) {
        switch (mode) {
            case Cloud:
                return EventProcessingOption.CLOUD;
            case Stream:
                return EventProcessingOption.STREAM;
            default:
                throw new IllegalArgumentException("Unknown mode " + mode);
        }
    }

    public static ClockType parseClockType(RuleConfig.ClockType clockType) {
        return ClockType.resolveClockType(clockType.getType());
    }
}
