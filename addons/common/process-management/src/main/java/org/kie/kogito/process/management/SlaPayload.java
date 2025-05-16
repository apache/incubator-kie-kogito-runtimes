package org.kie.kogito.process.management;

import org.kie.kogito.jobs.ExpirationTime;

public class SlaPayload {
    ExpirationTime expirationTime;

    public SlaPayload(ExpirationTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public ExpirationTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(ExpirationTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}
