package org.kie.kogito.rules.listeners;

import org.kie.kogito.rules.Match;

public interface AgendaListener {
    default void matchCreated() {};

    default void matchCancelled(/* cause */) {};

    default void beforeMatchFired(Match match) {};

    default void afterMatchFired(Match match) {};

    default void agendaGroupPopped(/* id */) {};

    default void agendaGroupPushed(/* id */) {};

}