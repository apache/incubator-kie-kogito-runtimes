package org.kie.kogito.tck.junit.extension;

import java.util.Optional;

import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.tck.junit.api.KogitoUnitTestContext;


public class ErrorKogitoUnitTestContextImpl implements KogitoUnitTestContext {

    private Throwable throwable;

    public ErrorKogitoUnitTestContextImpl(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public <T> T find(Class<T> clazz) {
        if(Throwable.class.isAssignableFrom(clazz)) {
            return clazz.cast(throwable);
        }
        return null;
    }

    @Override
    public Optional<ProcessInstance<? extends Model>> findById(String processId, String id) {
        return Optional.empty();
    }

    @Override
    public Optional<ProcessInstance<? extends Model>> findByBusinessKey(String processId, String businessKey) {
        return Optional.empty();
    }

}
