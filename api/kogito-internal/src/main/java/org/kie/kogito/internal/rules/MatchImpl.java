package org.kie.kogito.internal.rules;

import java.util.List;
import java.util.Objects;

import org.kie.api.definition.rule.Rule;
import org.kie.kogito.rules.Match;

public final class MatchImpl implements Match {

    public MatchImpl(Rule rule, List<Object> objects) {
        this.rule = rule;
        this.objects = objects;
    }

    private final Rule rule;
    private final List<Object> objects;

    @Override
    public Rule getRule() {
        return this.rule;
    }

    @Override
    public List<Object> getObjects() {
        return objects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MatchImpl match = (MatchImpl) o;
        return Objects.equals(rule, match.rule) &&
                Objects.equals(objects, match.objects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rule, objects);
    }
}
