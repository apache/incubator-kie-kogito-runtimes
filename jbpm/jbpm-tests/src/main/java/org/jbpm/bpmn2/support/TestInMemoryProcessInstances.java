package org.jbpm.bpmn2.support;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;

public class TestInMemoryProcessInstances<T> extends InMemoryProcessInstances<T> {

    private final ConcurrentMap<String, byte[]> removedInstances = new ConcurrentHashMap<>();

    public TestInMemoryProcessInstances(Process<T> process) {
        super(process);
    }

    @Override
    public void remove(String id) {
        byte[] data = getInstances().remove(id);
        if (data != null) {
            removedInstances.put(id, data);
        }
    }

    public Optional<ProcessInstance<T>> findDeletedById(String id) {
        byte[] data = removedInstances.remove(id);
        return Optional.of((ProcessInstance<T>) getMarshaller().unmarshallProcessInstance(data, getProcess(), true));
    }

}
