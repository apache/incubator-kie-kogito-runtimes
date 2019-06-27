/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen;

import java.util.concurrent.Future;

import org.junit.Test;
import org.kie.kogito.Executor;
import org.kie.kogito.codegen.data.AdultUnit;
import org.kie.kogito.codegen.data.Person;
import org.kie.kogito.codegen.data.Results;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitInstance;
import org.kie.kogito.rules.impl.RuleUnitRegistry;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RuleUnitCompilerTest extends AbstractCodegenTest {

    @Test
    public void testRuleUnit() throws Exception {
        generateCodeRulesOnly("org/kie/kogito/codegen/data/RuleUnit.drl");

        AdultUnit adults = new AdultUnit();

        Results results = new Results();
        adults.getResults().add( results );

        adults.getPersons().add(new Person( "Mario", 45 ));
        adults.getPersons().add(new Person( "Marilena", 47 ));
        adults.getPersons().add(new Person( "Sofia", 7 ));

        RuleUnit<AdultUnit> unit = RuleUnitRegistry.create(AdultUnit.class);
        RuleUnitInstance<AdultUnit> instance = unit.createInstance(adults);
        assertEquals(2, instance.fire() );

        assertTrue( results.getResults().containsAll( asList("Mario", "Marilena") ) );
    }


    @Test
    public void testRuleUnitExecutor() throws Exception {
        generateCodeRulesOnly("org/kie/kogito/codegen/data/RuleUnit.drl");

        AdultUnit adults = new AdultUnit();

        Results results = new Results();
        adults.getResults().add( results );

        adults.getPersons().add(new Person( "Mario", 45 ));
        adults.getPersons().add(new Person( "Marilena", 47 ));
        adults.getPersons().add(new Person( "Sofia", 7 ));

        RuleUnitInstance<AdultUnit> instance = RuleUnitRegistry.instance(adults);
        Executor executor = Executor.create();
        Future<Integer> done = executor.submit(instance);

        assertEquals(2, done.get().intValue() );

        assertTrue( results.getResults().containsAll( asList("Mario", "Marilena") ) );
    }


//    @Test
//    public void testRuleUnitWithOOPath() {
//        String str =
//                "import " + Person.class.getCanonicalName() + ";" +
//                "import " + AdultUnit.class.getCanonicalName() + "\n" +
//                "rule Adult @Unit( AdultUnit.class ) when\n" +
//                "    $p : /persons[age >= adultAge]\n" +
//                "then\n" +
//                "    results.add($p.getName());\n" +
//                "end\n";
//
//        KieContainer kieContainer = getKieContainer( null, str );
//        RuleUnitExecutor executor = kieContainer.newRuleUnitExecutor();
//
//        DataSource<Person> persons = DataSource.create( new Person( "Mario", 42 ),
//                                                        new Person( "Marilena", 44 ),
//                                                        new Person( "Sofia", 4 ) );
//
//        AdultUnit unit = new AdultUnit(persons);
//        assertEquals(2, executor.run( unit ) );
//
//        assertTrue( unit.getResults().containsAll( asList("Mario", "Marilena") ) );
//    }
//
//    public static class PositiveNegativeDTUnit implements RuleUnit {
//
//        private BigDecimal a_number;
//
//        private DataSource<BigDecimal> input1 = DataSource.create();
//        private DataSource<String> output1 = DataSource.create();
//
//        private String positive_or_negative;
//
//        public PositiveNegativeDTUnit(long val) {
//            this.a_number = new BigDecimal(val);
//        }
//
//        public DataSource<BigDecimal> getInput1() {
//            return input1;
//        }
//
//        public DataSource<String> getOutput1() {
//            return output1;
//        }
//
//        public String getPositive_or_negative() {
//            return positive_or_negative;
//        }
//
//        @Override
//        public void onStart() {
//            // input1: (simple assignment)
//            input1.insert(a_number);
//        }
//
//        @Override
//        public void onEnd() {
//            // output1: (simple assignment)
//            positive_or_negative = output1.iterator().next();
//        }
//
//        public String getResult() {
//            return getPositive_or_negative();
//        }
//    }
//
//    @Test
//    public void testWith2Rules() {
//        String str = "package " + this.getClass().getPackage().getName() + ";\n" +
//                "unit " + getCanonicalSimpleName(PositiveNegativeDTUnit.class) + ";\n" +
//                "import " + BigDecimal.class.getCanonicalName() + ";\n" +
//                "rule R1 \n" +
//                "when\n" +
//                "  BigDecimal( intValue() >= 0 ) from input1\n" +
//                "then\n" +
//                "  output1.insert(\"positive\");\n" +
//                "end\n" +
//                "rule R2 \n" +
//                "when\n" +
//                "  BigDecimal( intValue() < 0 ) from input1\n" +
//                "then\n" +
//                "  output1.insert(\"negative\");\n" +
//                "end\n";
//
//        KieContainer kieContainer = getKieContainer( null, str );
//        RuleUnitExecutor executor = kieContainer.newRuleUnitExecutor();
//
//        PositiveNegativeDTUnit ruleUnit = new PositiveNegativeDTUnit(47);
//        executor.run(ruleUnit);
//        assertEquals("positive", ruleUnit.getPositive_or_negative());
//
//        ruleUnit = new PositiveNegativeDTUnit(-999);
//        executor.run(ruleUnit);
//        assertEquals("negative", ruleUnit.getPositive_or_negative());
//    }
}
