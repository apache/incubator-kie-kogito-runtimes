package org.kie.kogito.rules;

import java.util.OptionalInt;

import org.kie.kogito.conf.ClockType;
import org.kie.kogito.conf.EventProcessingType;

public final class RuleUnitConfig {

    public static final RuleUnitConfig Default =
            new RuleUnitConfig(EventProcessingType.CLOUD, ClockType.REALTIME, null);

    private final EventProcessingType eventProcessingType;
    private final ClockType clockType;
    private final Integer sessionPool;

    public RuleUnitConfig(EventProcessingType eventProcessingType, ClockType clockType, Integer sessionPool) {
        this.eventProcessingType = eventProcessingType;
        this.clockType = clockType;
        this.sessionPool = sessionPool;
    }

    public EventProcessingType getEventProcessingType() {
        return eventProcessingType;
    }

    public ClockType getClockType() {
        return clockType;
    }

    public OptionalInt getSessionPool() {
        return (sessionPool == null) ? OptionalInt.empty() : OptionalInt.of(sessionPool);
    }
}
