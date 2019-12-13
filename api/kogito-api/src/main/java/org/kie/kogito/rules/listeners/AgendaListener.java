package org.kie.kogito.rules.listeners;

public interface AgendaListener {
    default void matchCreated() {};

    default void matchCancelled(/* cause */) {};

    default void beforeMatchFired() {};

    default void afterMatchFired() {};

    default void agendaGroupPopped(/* id */) {};

    default void agendaGroupPushed(/* id */) {};

}