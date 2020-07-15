package org.kogito;

import org.drools.core.WorkingMemoryEntryPoint;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.factmodel.traits.TraitTypeEnum;
import org.drools.core.rule.EntryPointId;
import org.drools.core.ruleunit.InternalStoreCallback;
import org.kie.kogito.rules.DataHandle;

public class KogitoDefaultFactHandle extends DefaultFactHandle implements KogitoInternalFactHandle {

    public KogitoDefaultFactHandle() {
    }

    public KogitoDefaultFactHandle(long id, Object object) {
        super(id, object);
    }

    public KogitoDefaultFactHandle(long id, Object object, long recency, WorkingMemoryEntryPoint wmEntryPoint) {
        super(id, object, recency, wmEntryPoint);
    }

    public KogitoDefaultFactHandle(long id, Object object, long recency, WorkingMemoryEntryPoint wmEntryPoint, boolean isTraitOrTraitable) {
        super(id, object, recency, wmEntryPoint, isTraitOrTraitable);
    }

    public KogitoDefaultFactHandle(long id, int identityHashCode, Object object, long recency, WorkingMemoryEntryPoint wmEntryPoint, boolean isTraitOrTraitable) {
        super(id, identityHashCode, object, recency, wmEntryPoint, isTraitOrTraitable);
    }

    public KogitoDefaultFactHandle(long id, int identityHashCode, Object object, long recency, EntryPointId entryPointId, boolean isTraitOrTraitable) {
        super(id, identityHashCode, object, recency, entryPointId, isTraitOrTraitable);
    }

    public KogitoDefaultFactHandle(long id, int identityHashCode, Object object, long recency, EntryPointId entryPointId, TraitTypeEnum traitType) {
        super(id, identityHashCode, object, recency, entryPointId, traitType);
    }

    public KogitoDefaultFactHandle(long id, String wmEntryPointId, int identityHashCode, int objectHashCode, long recency, Object object) {
        super(id, wmEntryPointId, identityHashCode, objectHashCode, recency, object);
    }

    private DataHandle dataHandle;
    private InternalStoreCallback dataStore;

    @Override
    public DataHandle getDataHandle() {
        return dataHandle;
    }

    @Override
    public void setDataHandle(DataHandle dataHandle) {
        this.dataHandle = dataHandle;
    }

    @Override
    public InternalStoreCallback getDataStore() {
        return dataStore;
    }

    @Override
    public void setDataStore( InternalStoreCallback dataStore ) {
        this.dataStore = dataStore;
    }
}
