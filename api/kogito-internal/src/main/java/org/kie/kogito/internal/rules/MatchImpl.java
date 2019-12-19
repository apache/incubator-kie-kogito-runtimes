/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
