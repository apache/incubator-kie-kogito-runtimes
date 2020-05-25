/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.process.casemgmt;

import java.util.Collection;

public class Stage extends ItemDescription {

    private final String activationExpression;
    private final String completionExpression;
    private final Boolean autoComplete;
    private final Collection<AdHocFragment> adHocFragments;

    public Stage(String id, String name, Status status, String activationExpression, String completionExpression, Boolean autoComplete, Collection<AdHocFragment> adHocFragments) {
        super(id, name, status);
        this.activationExpression = activationExpression;
        this.completionExpression = completionExpression;
        this.autoComplete = autoComplete;
        this.adHocFragments = adHocFragments;
    }

    public String getActivationExpression() {
        return activationExpression;
    }

    public String getCompletionExpression() {
        return completionExpression;
    }

    public Boolean getAutoComplete() {
        return autoComplete;
    }

    public Collection<AdHocFragment> getAdHocFragments() {
        return adHocFragments;
    }
}
