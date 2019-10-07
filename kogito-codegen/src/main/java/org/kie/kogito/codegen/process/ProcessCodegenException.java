package org.kie.kogito.codegen.process;

import java.text.MessageFormat;

public class ProcessCodegenException extends RuntimeException {

    public ProcessCodegenException(String path, Throwable cause) {
        super(MessageFormat.format("Could not process file \"{0}\": {1}", path, cause.getMessage()), cause);
    }
}
