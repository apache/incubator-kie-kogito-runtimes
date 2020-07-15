package org.drools.core.kogito.factory;

import org.drools.core.reteoo.KieComponentFactory;
import org.drools.core.spi.FactHandleFactory;

public class KogitoKieComponentFactory extends KieComponentFactory {

    @Override
    public FactHandleFactory getFactHandleFactoryService() {
        return new KogitoFactHandleFactory();
    }
}
