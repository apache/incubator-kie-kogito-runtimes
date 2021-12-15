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
package org.kie.kogito.addons.quarkus.knative.eventing.deployment;

import java.util.Collections;
import java.util.List;

import io.fabric8.knative.eventing.v1.Trigger;
import io.fabric8.knative.sources.v1.SinkBinding;
import io.quarkus.builder.item.SimpleBuildItem;

/**
 * Build item holder for generated Knative resources to bind to target Kogito service.
 */
public final class KogitoKnativeResourcesBuildItem extends SimpleBuildItem {

    private final List<SinkBinding> sinkBindings;
    private final List<Trigger> triggers;

    public KogitoKnativeResourcesBuildItem(final List<SinkBinding> sinkBindings, final List<Trigger> triggers) {
        this.sinkBindings = sinkBindings;
        this.triggers = triggers;
    }

    public List<SinkBinding> getSinkBindings() {
        return Collections.unmodifiableList(this.sinkBindings);
    }

    public List<Trigger> getTriggers() {
        return Collections.unmodifiableList(triggers);
    }
}
