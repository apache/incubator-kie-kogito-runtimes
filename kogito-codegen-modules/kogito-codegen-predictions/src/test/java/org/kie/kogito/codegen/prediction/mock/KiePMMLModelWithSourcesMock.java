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

import java.util.Collections;
import java.util.Map;

import org.kie.pmml.commons.model.HasSourcesMap;
import org.kie.pmml.commons.model.KiePMMLModel;

public class KiePMMLModelWithSourcesMock extends KiePMMLModel implements HasSourcesMap {

    private final Map<String, String> sourcesMap;
    private final Map<String, String> ruleSourcesMap;

    public KiePMMLModelWithSourcesMock(String name) {
        this(name, Collections.emptyMap(), null);
    }

    public KiePMMLModelWithSourcesMock(String name, Map<String, String> sourcesMap) {
        this(name, sourcesMap, null);
    }

    public KiePMMLModelWithSourcesMock(String name, Map<String, String> sourcesMap, Map<String, String> ruleSourcesMap) {
        super(name, Collections.emptyList());
        this.sourcesMap = sourcesMap;
        this.ruleSourcesMap = ruleSourcesMap;
    }

    @Override
    public Map<String, String> getSourcesMap() {
        return sourcesMap;
    }

    @Override
    public Map<String, String> getRulesSourcesMap() {
        return ruleSourcesMap;
    }

    @Override
    public void addSourceMap(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evaluate(Object o, Map<String, Object> map) {
        throw new UnsupportedOperationException();
    }
}
