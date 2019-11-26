package org.kie.kogito.codegen.rules;

public class RuleCodegenError extends Error {

    public RuleCodegenError() {
        super("Errors were generated during the code-generation process. Scroll up for details.");
    }
}
