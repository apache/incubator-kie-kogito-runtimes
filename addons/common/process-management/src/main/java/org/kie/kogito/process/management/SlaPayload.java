package org.kie.kogito.process.management;

import java.time.ZonedDateTime;

public class SlaPayload {
    private ZonedDateTime expirationTime;

    public SlaPayload() {
    }

    public SlaPayload(ZonedDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public ZonedDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(ZonedDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}
