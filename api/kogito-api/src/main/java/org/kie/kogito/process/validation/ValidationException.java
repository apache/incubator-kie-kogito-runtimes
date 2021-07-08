package org.kie.kogito.process.validation;

import java.util.Collection;
import java.util.Collections;

public class ValidationException extends RuntimeException {

    private final String processId;
    private final Collection<? extends ValidationError> errors;

    public ValidationException(String processId, Collection<? extends ValidationError> errors) {
        this.processId = processId;
        this.errors = Collections.unmodifiableCollection(errors);
    }

    public ValidationException(String processId, ValidationError error) {
        this(processId, Collections.singleton(error));
    }

    public ValidationException(String processId, String errorMessage) {
        this(processId, Collections.singleton(() -> errorMessage));
    }

    public Collection<? extends ValidationError> getErrors() {
        return errors;
    }

    public String getProcessId() {
        return processId;
    }
}
