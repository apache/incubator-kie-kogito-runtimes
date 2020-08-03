package org.kie.kogito.events.knative.ce.http;

/**
 * Simple structure to hold CloudEvent processing errors
 */
public class ResponseError {

    private String message;
    private String cause;

    public ResponseError() {

    }

    public ResponseError(String message) {
        this.message = message;
    }

    public ResponseError(String message, Throwable cause) {
        this.message = message;
        this.cause = cause.getMessage();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
