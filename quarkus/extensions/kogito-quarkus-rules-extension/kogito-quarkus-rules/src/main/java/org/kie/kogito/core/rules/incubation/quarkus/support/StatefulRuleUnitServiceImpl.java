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

package org.kie.kogito.core.rules.incubation.quarkus.support;

import org.kie.kogito.incubation.common.*;
import org.kie.kogito.incubation.common.objectmapper.InternalObjectMapper;
import org.kie.kogito.incubation.rules.QueryId;
import org.kie.kogito.incubation.rules.RuleUnitId;
import org.kie.kogito.incubation.rules.RuleUnitInstanceId;
import org.kie.kogito.incubation.rules.services.StatefulRuleUnitService;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitData;
import org.kie.kogito.rules.RuleUnitInstance;
import org.kie.kogito.rules.RuleUnits;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

class StatefulRuleUnitServiceImpl implements StatefulRuleUnitService {

    private final RuleUnits ruleUnits;

    public StatefulRuleUnitServiceImpl(RuleUnits ruleUnits) {
        this.ruleUnits = ruleUnits;
    }

    public Stream<DataContext> evaluate(Id id, DataContext inputContext) {
        RuleUnitId ruleUnitId;
        QueryId queryId;
        if (id instanceof QueryId) {
            queryId = (QueryId) id;
            ruleUnitId = queryId.ruleUnitId();
        } else {
            // LocalDecisionId.parse(decisionId);
            throw new IllegalArgumentException(
                    "Not a valid query id " + id.toLocalId());
        }

        Map<String, Object> payload = inputContext.as(MapDataContext.class).toMap();
        RuleUnitData ruleUnitData = this.convertValue(payload, ruleUnitId);
        RuleUnit<RuleUnitData> ruleUnit = ruleUnits.create((Class<RuleUnitData>) ruleUnitData.getClass());
        RuleUnitInstance<RuleUnitData> instance = ruleUnit.createInstance(ruleUnitData);
        List<Map<String, Object>> results = instance.executeQuery(queryId.queryId());

        return results.stream().map(MapDataContext::of);

    }

    private RuleUnitData convertValue(Map<String, Object> payload, RuleUnitId ruleUnitId) {
        try {
            // converts the identifier into a Class object for conversion
            Class<RuleUnitData> type = (Class<RuleUnitData>) Thread.currentThread().getContextClassLoader().loadClass(ruleUnitId.ruleUnitId());
            return InternalObjectMapper.objectMapper().convertValue(payload, type);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot load class " + ruleUnitId.ruleUnitId(), e);
        }
    }

    @Override
    public MetaDataContext create(LocalId localId, ExtendedDataContext extendedDataContext) {
        RuleUnitId ruleUnitId;
        if (localId instanceof RuleUnitId) {
            ruleUnitId = (RuleUnitId) localId;
        } else throw new IllegalArgumentException("cannot parse rule unit id");

        DataContext inputContext = extendedDataContext.data();

        Map<String, Object> payload = inputContext.as(MapDataContext.class).toMap();
        RuleUnitData ruleUnitData = this.convertValue(payload, ruleUnitId);
        RuleUnit<RuleUnitData> ruleUnit = ruleUnits.create((Class<RuleUnitData>) ruleUnitData.getClass());
        RuleUnitInstance<RuleUnitData> instance = ruleUnit.createInstance(ruleUnitData);
        String instanceId = UUID.randomUUID().toString();
        ruleUnits.register(instanceId, instance);
        RuleUnitInstanceId ruleUnitInstanceId = ruleUnitId.instances().get(instanceId);
        return MapDataContext.of(Map.of("id", ruleUnitInstanceId.asLocalUri().path()));
    }

    @Override
    public MetaDataContext dispose(LocalId localId) {
        RuleUnitInstanceId ruleUnitInstanceId;
        if (localId instanceof RuleUnitInstanceId) {
            ruleUnitInstanceId = (RuleUnitInstanceId) localId;
        } else throw new IllegalArgumentException("cannot parse rule unit id");
        RuleUnitInstance<?> instance = ruleUnits.getRegisteredInstance(ruleUnitInstanceId.ruleUnitInstanceId());
        if (instance == null) throw new IllegalArgumentException("Unknown instance " + localId);
        instance.dispose();
        return EmptyMetaDataContext.Instance;
    }

    @Override
    public MetaDataContext fire(LocalId localId) {
        RuleUnitInstanceId ruleUnitInstanceId;
        if (localId instanceof RuleUnitInstanceId) {
            ruleUnitInstanceId = (RuleUnitInstanceId) localId;
        } else throw new IllegalArgumentException("cannot parse rule unit id");
        RuleUnitInstance<?> instance = ruleUnits.getRegisteredInstance(ruleUnitInstanceId.ruleUnitInstanceId());
        instance.fire();
        return EmptyMetaDataContext.Instance;
    }

    @Override
    public Stream<ExtendedDataContext> query(LocalId localId, ExtendedDataContext params) {
        RuleUnitInstanceId ruleUnitInstanceId;
        // must add a QueryId for instances!
//        QueryId queryId;
//        if (localId instanceof QueryId) {
//            queryId = (QueryId) localId;
//            ruleUnitId = queryId.ruleUnitId();
//        } else {
//            // LocalDecisionId.parse(decisionId);
//            throw new IllegalArgumentException(
//                    "Not a valid query id " + id.toLocalId());
//        }
//
//        Map<String, Object> payload = inputContext.as(MapDataContext.class).toMap();
//        RuleUnitData ruleUnitData = this.convertValue(payload, ruleUnitId);
//        RuleUnit<RuleUnitData> ruleUnit = ruleUnits.create((Class<RuleUnitData>) ruleUnitData.getClass());
//        RuleUnitInstance<RuleUnitData> instance = ruleUnit.createInstance(ruleUnitData);
//        List<Map<String, Object>> results = instance.executeQuery(queryId.queryId());
//
//        return results.stream().map(MapDataContext::of);
        throw new UnsupportedOperationException();

    }

}
