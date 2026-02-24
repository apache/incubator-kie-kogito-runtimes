package org.kie.kogito.services.uow;

import java.util.function.Supplier;

import org.kie.kogito.uow.UnitOfWorkManager;

public abstract class UnitOfWorkExecutor {

    private static UnitOfWorkExecutor unitOfWorkExecutor;

    public static void set(UnitOfWorkExecutor executor) {
        unitOfWorkExecutor = executor;
    }

    private static UnitOfWorkExecutor getExecutor() {
        if (unitOfWorkExecutor == null) {
            unitOfWorkExecutor = new DefaultUnitOfWorkExecutor();
        }
        return unitOfWorkExecutor;
    }

    public static <T> T executeInUnitOfWork(UnitOfWorkManager uowManager, Supplier<T> supplier) {
        return getExecutor().execute(uowManager, supplier);
    }

    abstract <T> T execute(UnitOfWorkManager uowManager, Supplier<T> supplier);
}
