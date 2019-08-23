package org.kie.kogito.application.stubs;

public class ApplicationStub {
    public Object create() {
        throw new NoClassDefFoundError(
                "The Java linker has picked up the default ApplicationStub. " +
                        "This should not happen: the default ApplicationStub is excluded from the Jar.");
    }
}
