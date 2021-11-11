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
package org.kie.kogito.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.drools.core.common.ReteEvaluator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kie.api.runtime.KieSession;
import org.kie.api.time.SessionPseudoClock;
import org.kie.internal.builder.conf.PropertySpecificOption;
import org.kie.kogito.Application;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.data.Address;
import org.kie.kogito.codegen.data.Person;
import org.kie.kogito.codegen.data.StockTick;
import org.kie.kogito.codegen.data.ValueDrop;
import org.kie.kogito.codegen.rules.RuleCodegenError;
import org.kie.kogito.codegen.rules.multiunit.MultiUnit;
import org.kie.kogito.codegen.rules.singleton.Datum;
import org.kie.kogito.codegen.rules.singleton.Singleton;
import org.kie.kogito.codegen.unit.AdultUnit;
import org.kie.kogito.codegen.unit.PersonsUnit;
import org.kie.kogito.codegen.unit.StockUnit;
import org.kie.kogito.rules.DataHandle;
import org.kie.kogito.rules.DataObserver;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitInstance;
import org.kie.kogito.rules.RuleUnitQuery;
import org.kie.kogito.rules.RuleUnits;
import org.kie.kogito.rules.units.AbstractRuleUnitInstance;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.kie.kogito.codegen.rules.RuleUnitGenerator.KOGITO_USE_LEGACY_SESSION;

public class RuleUnitCompilerIT extends AbstractCodegenIT {

    private enum SessionType {
        LEGACY,
        LIGHTWEIGHT
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void testRuleUnit(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType, "org/kie/kogito/codegen/unit/RuleUnit.drl");

        AdultUnit adults = new AdultUnit();

        adults.getPersons().add(new Person("Mario", 45));
        adults.getPersons().add(new Person("Marilena", 47));

        Person sofia = new Person("Sofia", 7);
        DataHandle dhSofia = adults.getPersons().add(sofia);

        RuleUnit<AdultUnit> unit = application.get(RuleUnits.class).create(AdultUnit.class);
        RuleUnitInstance<AdultUnit> instance = unit.createInstance(adults);

        if (sessionType == SessionType.LEGACY) {
            assertTrue(((AbstractRuleUnitInstance) instance).getEvaluator() instanceof KieSession);
        } else {
            assertTrue(((AbstractRuleUnitInstance) instance).getEvaluator() instanceof ReteEvaluator);
        }

        assertTrue(instance.getClock() instanceof SessionPseudoClock);

        assertEquals(2, instance.fire());
        assertTrue(adults.getResults().getResults().containsAll(asList("Mario", "Marilena")));

        sofia.setAge(22);
        adults.getPersons().update(dhSofia, sofia);
        assertEquals(1, instance.fire());
        assertTrue(adults.getResults().getResults().containsAll(asList("Mario", "Marilena", "Sofia")));
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void testRuleUnitModify(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType, "org/kie/kogito/codegen/unit/RuleUnitModify.drl");

        AdultUnit adults = new AdultUnit();

        Person sofia = new Person("Sofia", 7);
        DataHandle dhSofia = adults.getPersons().add(sofia);

        RuleUnit<AdultUnit> unit = application.get(RuleUnits.class).create(AdultUnit.class);
        RuleUnitInstance<AdultUnit> instance = unit.createInstance(adults);

        assertEquals(2, instance.fire());

        assertTrue(adults.getResults().getResults().containsAll(asList("Sofia")));
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void testRuleUnitDelete(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType, "org/kie/kogito/codegen/unit/RuleUnitDelete.drl");

        AdultUnit adults = new AdultUnit();

        adults.getPersons().add(new Person("Mario", 45));
        adults.getPersons().add(new Person("Marilena", 47));
        adults.getPersons().add(new Person("Sofia", 7));

        RuleUnit<AdultUnit> unit = application.get(RuleUnits.class).create(AdultUnit.class);
        RuleUnitInstance<AdultUnit> instance = unit.createInstance(adults);

        instance.fire();

        List<String> results = adults.getResults().getResults();
        assertEquals(2, results.size());
        assertTrue(results.containsAll(asList("Mario", "Marilena")));
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void testRuleUnitQuery(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType, "org/kie/kogito/codegen/unit/RuleUnitQuery.drl");

        AdultUnit adults = new AdultUnit();

        adults.getPersons().add(new Person("Mario", 45));
        adults.getPersons().add(new Person("Marilena", 47));
        adults.getPersons().add(new Person("Sofia", 7));

        RuleUnit<AdultUnit> unit = application.get(RuleUnits.class).create(AdultUnit.class);
        RuleUnitInstance<AdultUnit> instance = unit.createInstance(adults);

        Class<? extends RuleUnitQuery<List<String>>> queryClass = (Class<? extends RuleUnitQuery<List<String>>>) application.getClass()
                .getClassLoader().loadClass("org.kie.kogito.codegen.unit.AdultUnitQueryFindAdults");

        List<String> results = instance.executeQuery(queryClass);

        assertEquals(2, results.size());
        assertTrue(results.containsAll(asList("Mario", "Marilena")));
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void testRuleUnitQueryOnPrimitive(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType, "org/kie/kogito/codegen/unit/RuleUnitQuery.drl");

        AdultUnit adults = new AdultUnit();

        adults.getPersons().add(new Person("Mario", 45));
        adults.getPersons().add(new Person("Marilena", 47));
        adults.getPersons().add(new Person("Sofia", 7));

        RuleUnit<AdultUnit> unit = application.get(RuleUnits.class).create(AdultUnit.class);
        RuleUnitInstance<AdultUnit> instance = unit.createInstance(adults);

        List<Integer> results = instance.executeQuery("FindAdultsAge")
                .stream()
                .map(m -> m.get("$age"))
                .map(Integer.class::cast)
                .collect(toList());

        assertEquals(2, results.size());
        assertTrue(results.containsAll(asList(45, 47)));
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void testRuleUnitQueryWithNoRules(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType,
                "org/kie/kogito/codegen/unit/RuleUnitQueryNoRules.drl");

        AdultUnit adults = new AdultUnit();

        adults.getPersons().add(new Person("Mario", 45).setAdult(true));
        adults.getPersons().add(new Person("Marilena", 47).setAdult(true));
        adults.getPersons().add(new Person("Sofia", 7).setAdult(true));

        RuleUnit<AdultUnit> unit = application.get(RuleUnits.class).create(AdultUnit.class);
        RuleUnitInstance<AdultUnit> instance = unit.createInstance(adults);

        List<Integer> results = instance.executeQuery("FindAdultsAge")
                .stream()
                .map(m -> m.get("$sum"))
                .map(Integer.class::cast)
                .collect(toList());

        assertEquals(1, results.size());
        assertThat(results).containsExactlyInAnyOrder(99);
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void testRuleUnitExecutor(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType,
                "org/kie/kogito/codegen/unit/RuleUnit.drl",
                "org/kie/kogito/codegen/unit/PersonsUnit.drl");

        DataStore<Person> persons = DataSource.createStore();
        persons.add(new Person("Mario", 45));
        persons.add(new Person("Marilena", 17));
        persons.add(new Person("Sofia", 7));

        RuleUnit<AdultUnit> adultUnit = application.get(RuleUnits.class).create(AdultUnit.class);

        AdultUnit adultData18 = new AdultUnit(persons, 18);
        RuleUnitInstance<AdultUnit> adultUnitInstance18 = adultUnit.createInstance(adultData18, "adult18");

        AdultUnit adultData21 = new AdultUnit(persons, 21);
        RuleUnitInstance<AdultUnit> adultUnitInstance21 = adultUnit.createInstance(adultData21, "adult21");

        RuleUnit<PersonsUnit> personsUnit = application.get(RuleUnits.class).create(PersonsUnit.class);
        personsUnit.createInstance(new PersonsUnit(persons)).fire();

        assertEquals(2, adultData18.getResults().getResults().size());
        assertTrue(adultData18.getResults().getResults().containsAll(asList("Mario", "Marilena")));
        assertEquals(1, adultData21.getResults().getResults().size());
        assertTrue(adultData21.getResults().getResults().containsAll(asList("Mario")));
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void generateSinglePackageSingleUnit(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType,
                "org/kie/kogito/codegen/rules/multiunit/MultiUnit.drl",
                "org/kie/kogito/codegen/rules/multiunit/MultiUnit2.drl");

        ArrayList<String> strings = new ArrayList<>();

        RuleUnit<MultiUnit> mu = application.get(RuleUnits.class).create(MultiUnit.class);
        MultiUnit data = new MultiUnit();
        RuleUnitInstance<MultiUnit> instance = mu.createInstance(data);
        data.getValues().subscribe(DataObserver.of(v -> {
            if (v != null)
                strings.add((String) v);
        }));
        data.getValues().add("start");
        instance.fire();

        assertEquals(asList("start", "middle", "done"), strings);

    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void singletonStore(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType, "org/kie/kogito/codegen/rules/singleton/Singleton.drl");

        ArrayList<String> data = new ArrayList<>();
        AtomicReference<Datum> lastSeen = new AtomicReference<>();

        RuleUnit<Singleton> mu = application.get(RuleUnits.class).create(Singleton.class);
        Singleton unitData = new Singleton();
        RuleUnitInstance<Singleton> instance = mu.createInstance(unitData);
        unitData.getOutput().subscribe(
                DataObserver.ofUpdatable(v -> data.add(v == null ? null : v.getValue())));
        unitData.getOutput().subscribe(
                DataObserver.of(lastSeen::set));

        unitData.getInput().set(new Datum("start"));
        instance.fire();
        assertEquals(asList("continue", "updated", null, "done"), data);

        lastSeen.get().setValue("updated");
        unitData.getOutput().update();
        instance.fire();

        assertEquals(asList("continue", "updated", null, "done", "updated", null, "done"), data);

    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void test2PatternsOopath(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType, "org/kie/kogito/codegen/unit/TwoPatternsQuery.drl");

        AdultUnit adults = new AdultUnit();

        Person mario = new Person("Mario", 42);
        mario.addAddress(new Address("Milano"));
        Person mark = new Person("Mark", 40);
        mark.addAddress(new Address("London"));
        Person edson = new Person("Edson", 37);
        edson.addAddress(new Address("Toronto"));

        adults.getPersons().add(mario);
        adults.getPersons().add(mark);
        adults.getPersons().add(edson);

        RuleUnit<AdultUnit> unit = application.get(RuleUnits.class).create(AdultUnit.class);
        RuleUnitInstance<AdultUnit> instance = unit.createInstance(adults);

        List<Person> results = instance.executeQuery("FindPeopleInMilano")
                .stream()
                .map(m -> m.get("$p"))
                .map(Person.class::cast)
                .collect(toList());

        assertEquals(1, results.size());
        assertEquals("Mario", results.get(0).getName());
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void testRuleUnitWithNoBindQueryShouldntCompile(SessionType sessionType) throws Exception {
        try {
            Application application = createApplication(sessionType, "org/kie/kogito/codegen/unit/RuleUnitNoBindQuery.drl");
            fail("A query without binding shouldn't compile");
        } catch (RuleCodegenError e) {
            // ignore
        }
    }

    @ParameterizedTest
    @EnumSource(SessionType.class)
    public void testCep(SessionType sessionType) throws Exception {
        Application application = createApplication(sessionType, "org/kie/kogito/codegen/unit/Stock.drl");

        StockUnit stockUnit = new StockUnit();
        RuleUnit<StockUnit> unit = application.get(RuleUnits.class).create(StockUnit.class);
        RuleUnitInstance<StockUnit> instance = unit.createInstance(stockUnit);

        stockUnit.getStockTicks().append(new StockTick("IBM", 2000, 100));
        stockUnit.getStockTicks().append(new StockTick("IBM", 1700, 170));
        stockUnit.getStockTicks().append(new StockTick("IBM", 1500, 240));

        ValueDrop valueDrop = (ValueDrop) instance.executeQuery("highestValueDrop", "IBM").get(0).get("$s");
        assertEquals(300, valueDrop.getDropAmount());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void checckPropertyReactvity(boolean usePropertyReactivity) throws Exception {
        // KOGITO-5101
        KogitoBuildContext context = newContext();
        if (!usePropertyReactivity) {
            context.setApplicationProperty(PropertySpecificOption.PROPERTY_NAME, PropertySpecificOption.DISABLED.toString());
        }

        Application application = createApplication(context, "org/kie/kogito/codegen/unit/RuleUnitNoPropReact.drl");

        AdultUnit adults = new AdultUnit();

        Person mario = new Person("Mario", 45);
        adults.getPersons().add(mario);

        RuleUnit<AdultUnit> unit = application.get(RuleUnits.class).create(AdultUnit.class);
        RuleUnitInstance<AdultUnit> instance = unit.createInstance(adults);

        instance.fire();

        assertEquals(usePropertyReactivity ? 46 : 50, mario.getAge());
    }

    private Application createApplication(SessionType sessionType, String... drls) throws Exception {
        KogitoBuildContext context = newContext();
        context.setApplicationProperty(KOGITO_USE_LEGACY_SESSION, "" + (sessionType == SessionType.LEGACY));
        return createApplication(context, drls);
    }

    private Application createApplication(KogitoBuildContext context, String... drls) throws Exception {
        Map<TYPE, List<String>> resourcesTypeMap = new HashMap<>();
        resourcesTypeMap.put(TYPE.RULES, Arrays.asList(drls));
        Application application = generateCode(resourcesTypeMap, context);
        return application;
    }
}
