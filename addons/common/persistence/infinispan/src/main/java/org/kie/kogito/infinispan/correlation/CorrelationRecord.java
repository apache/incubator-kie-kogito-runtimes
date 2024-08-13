package org.kie.kogito.infinispan.correlation;

public record CorrelationRecord(String encodedCorrelationId, String correlatedId,
                                String correlation) {

    @Override
    public String toString() {
        return "InfinispanCorrelation[" +
                "encodedCorrelationId=" + encodedCorrelationId + ", " +
                "correlatedId=" + correlatedId + ", " +
                "correlation=" + correlation + ']';
    }

}
