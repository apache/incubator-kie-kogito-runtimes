package org.drools.core.ruleunit.impl;

import org.drools.core.spi.Activation;
import org.drools.core.util.bitmask.BitMask;
import org.kie.kogito.rules.DataEvent;
import org.kie.kogito.rules.DataHandle;

public class EntryPointUpdate<T> extends DataEvent.Update<T> {

    private final BitMask mask;
    private final Class<?> modifiedClass;
    private final Activation activation;

    public EntryPointUpdate(DataHandle handle,
                            T value,
                            BitMask mask,
                            Class<?> modifiedClass,
                            Activation activation) {
        super(handle, value);

        this.mask = mask;
        this.modifiedClass = modifiedClass;
        this.activation = activation;
    }

    public BitMask mask() {
        return mask;
    }

    public Class<?> modifiedClass() {
        return modifiedClass;
    }

    public Activation activation() {
        return activation;
    }
}