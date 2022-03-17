package org.kie.kogito.services.event.impl;

class DummyEvent {

    private String dummyField;

    @SuppressWarnings("unused")
    public DummyEvent() {
    }

    public DummyEvent(String dummyField) {
        this.dummyField = dummyField;
    }

    @SuppressWarnings("unused")
    public String getDummyField() {
        return dummyField;
    }
}
