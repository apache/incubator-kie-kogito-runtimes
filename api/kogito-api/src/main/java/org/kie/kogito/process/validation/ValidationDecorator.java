package org.kie.kogito.process.validation;

public abstract class ValidationDecorator {

    protected final ValidationException exception;

    public ValidationDecorator(ValidationException exception) {
        this.exception = exception;
    }

    public abstract ValidationDecorator decorate();

    public ValidationException exception() {
        throw exception;
    }

    public String simpleMessage() {
        return "Error during validation for process: " + exception.getProcessId();
    }
}
