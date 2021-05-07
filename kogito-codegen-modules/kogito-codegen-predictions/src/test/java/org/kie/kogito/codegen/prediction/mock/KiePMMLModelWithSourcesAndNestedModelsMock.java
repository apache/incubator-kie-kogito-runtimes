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

package org.kie.kogito.codegen.prediction.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kie.pmml.commons.model.HasNestedModels;
import org.kie.pmml.commons.model.KiePMMLModel;

public class KiePMMLModelWithSourcesAndNestedModelsMock extends KiePMMLModelWithSourcesMock implements HasNestedModels {

    private final List<KiePMMLModel> nestedModels;

    public KiePMMLModelWithSourcesAndNestedModelsMock(String name, KiePMMLModel... nestedModels) {
        super(name);
        this.nestedModels = Arrays.asList(nestedModels);
    }

    public KiePMMLModelWithSourcesAndNestedModelsMock(String name, Map<String, String> sourcesMap, KiePMMLModel... nestedModels) {
        super(name, sourcesMap);
        this.nestedModels = Arrays.asList(nestedModels);
    }

    public KiePMMLModelWithSourcesAndNestedModelsMock(String name, Map<String, String> sourcesMap, Map<String, String> ruleSourcesMap, KiePMMLModel... nestedModels) {
        super(name, sourcesMap, ruleSourcesMap);
        this.nestedModels = Arrays.asList(nestedModels);
    }

    @Override
    public List<KiePMMLModel> getNestedModels() {
        return nestedModels;
    }
}
