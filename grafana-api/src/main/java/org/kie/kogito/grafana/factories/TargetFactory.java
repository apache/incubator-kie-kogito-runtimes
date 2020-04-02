/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.grafana.factories;

import java.util.ArrayList;
import java.util.List;

import org.kie.kogito.grafana.model.panel.GrafanaTarget;

public class TargetFactory {

    private TargetFactory() {
    }

    static List<GrafanaTarget> createTargets(String expr) {
        List<GrafanaTarget> targets = new ArrayList<>();
        targets.add(new GrafanaTarget(expr, "time_series"));
        return targets;
    }
}
