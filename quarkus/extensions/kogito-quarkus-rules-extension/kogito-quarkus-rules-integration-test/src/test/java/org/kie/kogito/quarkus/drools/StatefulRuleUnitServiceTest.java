package org.kie.kogito.quarkus.drools;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kie.kogito.incubation.application.AppRoot;
import org.kie.kogito.incubation.common.EmptyDataContext;
import org.kie.kogito.incubation.common.ExtendedDataContext;
import org.kie.kogito.incubation.common.MapDataContext;
import org.kie.kogito.incubation.common.MetaDataContext;
import org.kie.kogito.incubation.rules.InstanceQueryId;
import org.kie.kogito.incubation.rules.RuleUnitIds;
import org.kie.kogito.incubation.rules.services.StatefulRuleUnitService;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class StatefulRuleUnitServiceTest {
    @Inject
    AppRoot appRoot;
    @Inject
    StatefulRuleUnitService ruleUnitService;

    @Test
    void testCreate() {
        var id = appRoot.get(RuleUnitIds.class).get(AnotherService.class);
        MetaDataContext result = ruleUnitService.create(id, ExtendedDataContext.ofData(new AnotherService()));

        String instanceId = MapDataContext.from(result).get("id").toString();
        assertEquals("/rule-units/org.kie.kogito.quarkus.drools.AnotherService/instances",
                instanceId.substring(0, instanceId.lastIndexOf('/')));
    }

    @Test
    void testQuery() {
        var id = appRoot.get(RuleUnitIds.class).get(AnotherService.class);
        var ruleUnitData = new AnotherService();
        MetaDataContext created = ruleUnitService.create(id, ExtendedDataContext.ofData(ruleUnitData));
        String instanceId = MapDataContext.from(created).get("id").toString();
        String prefix = instanceId.substring(0, instanceId.lastIndexOf('/'));
        String ruid = instanceId.substring(instanceId.lastIndexOf('/')+1);
        InstanceQueryId queryId = id.instances().get(ruid).queries().get("Strings");

        ruleUnitData.getStrings().add(new StringHolder("hello folks"));
        ruleUnitData.getStrings().add(new StringHolder("hello people"));
        ruleUnitData.getStrings().add(new StringHolder("hello Mario"));
        ruleUnitData.getStrings().add(new StringHolder("helicopter"));

        Stream<ExtendedDataContext> result = ruleUnitService.query(queryId, ExtendedDataContext.ofData(EmptyDataContext.Instance));
        List<String> strings = result
                .map(e -> e.data().as(MapDataContext.class).get("results", StringHolder.class).getValue())
                .collect(Collectors.toList());

        strings.removeAll(List.of("hello folks", "hello people", "hello Mario"));
        assertTrue(strings.isEmpty());

    }

}
