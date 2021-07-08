package org.kie.kogito.process.validation;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationLogDecorator extends ValidationDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationLogDecorator.class);

    public ValidationLogDecorator(ValidationException exception) {
        super(exception);
    }

    public static ValidationLogDecorator of(ValidationException exception){
        return new ValidationLogDecorator(exception);
    }

    @Override
    public ValidationLogDecorator decorate() {
        String message = exception.getErrors()
                .stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.joining("\n - ", " - ", ""));
        LOGGER.error("Invalid process: '{}'. Found errors:\n{}\n", exception.getProcessId(), message);
        return this;
    }
}
