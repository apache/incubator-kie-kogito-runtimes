package org.kie.kogito.codegen.decision.events;

import java.util.Objects;

import org.kie.kogito.event.CloudEventMeta;
import org.kie.kogito.event.EventKind;

/**
 * Decisions representation of {@link CloudEventMeta} with inner information about the generated DMN model.
 */
public class DecisionCloudEventMeta extends CloudEventMeta {

    final String methodNameChunk;

    public DecisionCloudEventMeta(String type, String source, EventKind kind, String methodNameChunk) {
        super(type, source, kind);
        this.methodNameChunk = methodNameChunk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DecisionCloudEventMeta that = (DecisionCloudEventMeta) o;
        return getType().equals(that.getType()) && getSource().equals(that.getSource()) && getKind() == that.getKind();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getSource(), getKind());
    }
}
