/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.incubation.rules;

import org.kie.kogito.incubation.application.LocalId;
import org.kie.kogito.incubation.application.LocalUriId;

public class QueryId extends LocalUriId implements LocalId {
    public static final String PREFIX = "queries";

    private final RuleUnitId ruleUnitId;
    private final String queryId;

    public QueryId(RuleUnitId ruleUnitId, String queryId) {
        super(ruleUnitId.asLocalUri().append(PREFIX).append(queryId));
        LocalId localId = ruleUnitId.toLocalId();
        if (!localId.asLocalUri().startsWith(RuleUnitId.PREFIX)) {
            throw new InvalidRuleUnitIdException(localId); // fixme use typed exception
        }

        this.ruleUnitId = ruleUnitId;
        this.queryId = queryId;
    }

    @Override
    public LocalId toLocalId() {
        return this;
    }

    public RuleUnitId ruleUnitId() {
        return ruleUnitId;
    }

    public String queryId() {
        return queryId;
    }

}
