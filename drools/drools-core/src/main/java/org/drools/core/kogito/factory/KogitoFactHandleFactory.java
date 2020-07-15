package org.drools.core.kogito.factory;

import org.drools.core.WorkingMemoryEntryPoint;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.ObjectTypeConf;
import org.drools.core.reteoo.ReteooFactHandleFactory;

public class KogitoFactHandleFactory extends ReteooFactHandleFactory {

    @Override
    public InternalFactHandle newFactHandle(long id, Object object, long recency, ObjectTypeConf conf, InternalWorkingMemory workingMemory, WorkingMemoryEntryPoint wmEntryPoint) {
        return new KogitoDefaultFactHandle(id, object, recency, wmEntryPoint);
    }
}