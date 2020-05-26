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

import java.util.ArrayList;
import java.util.Collection;

import org.kie.api.definition.process.Node;

public class Stage extends ItemDescription {

    private final String activationExpression;
    private final String completionExpression;
    private final Boolean autoComplete;
    private final Collection<AdHocFragment> adHocFragments;

    private Stage(String id, String name, Status status, String activationExpression, String completionExpression, Boolean autoComplete, Collection<AdHocFragment> adHocFragments) {
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

    public static class Builder {
        private String id;
        private String name;
        private Status status;
        private String activationExpression;
        private String completionExpression;
        private Boolean autoComplete;
        private Collection<AdHocFragment> fragments;

        public Builder(String id) {
            this.id = id;
        }

        public Builder(Stage stage) {
            this.id = stage.getId();
            this.name = stage.getName();
            this.status = stage.getStatus();
            this.activationExpression = stage.getActivationExpression();
            this.completionExpression = stage.getCompletionExpression();
            this.autoComplete = stage.getAutoComplete();
            this.fragments = stage.getAdHocFragments();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder withActivationExpression(String activationExpression) {
            this.activationExpression = activationExpression;
            return this;
        }

        public Builder withCompletionExpression(String completionExpression) {
            this.completionExpression = completionExpression;
            return this;
        }

        public Builder withAutoComplete(Boolean autoComplete) {
            this.autoComplete = autoComplete;
            return this;
        }

        public Builder withFragment(Node node) {
            return withFragment(node, node.getName());
        }

        public Builder withFragment(Node node, String name) {
            if (this.fragments == null) {
                this.fragments = new ArrayList<>();
            }
            this.fragments.add(new AdHocFragment(node.getClass().getSimpleName(), name));
            return this;
        }

        public Stage build() {
            return new Stage(id, name, status, activationExpression, completionExpression, autoComplete, fragments);
        }
    }
}
