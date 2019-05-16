/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.drools.compiler.integrationtests.concurrency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.utils.KieHelper;

public class ConcurrentInsertionsToSubnetworksTest {

    static Stream<Arguments> parameters() {
        return Stream.of(sharedSubnetworkAccumulateRule,
                         sharedSubnetworkExistsRule,
                         sharedSubnetworkNotRule,
                         noSharingSubnetworkAccumulateRule)
                .map(Arguments::arguments);
    }

    private final static String sharedSubnetworkAccumulateRule =
            "import " + AtomicInteger.class.getCanonicalName() + ";\n" +
                    "rule R1y when\n" +
                    "    Number() from accumulate ( AtomicInteger() and $s : String( this == \"test_1\" ) ; count($s)" +
                    " )" +
                    "    AtomicInteger() \n" +
                    "    Long()\n" +
                    "then\n" +
                    "    System.out.println(\"R1y\");" +
                    "end\n" +
                    "\n" +
                    "rule R1x when\n" +
                    "    Number() from accumulate ( AtomicInteger() and $s : String( this == \"test_1\" ) ; count($s)" +
                    " )\n" +
                    "    AtomicInteger( get() == 1 ) \n" +
                    "then\n" +
                    "    System.out.println(\"R1x\");" +
                    "end\n" +
                    "" +
                    "rule R2 when\n" +
                    "    $i : AtomicInteger( get() < 3 )\n" +
                    "then\n" +
                    "    System.out.println(\"R2\");" +
                    "    $i.incrementAndGet();" +
                    "    update($i);" +
                    "end\n";
    private final static String noSharingSubnetworkAccumulateRule =
            "import " + AtomicInteger.class.getCanonicalName() + ";\n" +
                    "rule R1y when\n" +
                    "    AtomicInteger() \n" +
                    "    Number() from accumulate ( AtomicInteger() and $s : String( this == \"test_1\" ) ; count($s)" +
                    " )" +
                    "    Long()\n" +
                    "then\n" +
                    "    System.out.println(\"R1y\");" +
                    "end\n" +
                    "\n" +
                    "rule R1x when\n" +
                    "    AtomicInteger() \n" +
                    "    Number() from accumulate ( $i : AtomicInteger( get() == 1) and String( this == \"test_2\" ) " +
                    "; count($i) )\n" +
                    "then\n" +
                    "    System.out.println(\"R1x\");" +
                    "end\n" +
                    "" +
                    "rule R2 when\n" +
                    "    $i : AtomicInteger( get() < 3 )\n" +
                    "then\n" +
                    "    System.out.println(\"R2\");" +
                    "    $i.incrementAndGet();" +
                    "    update($i);" +
                    "end\n";
    private final static String sharedSubnetworkNotRule =
            "import " + AtomicInteger.class.getCanonicalName() + ";\n" +
                    "rule R1 when\n" +
                    "    AtomicInteger() \n" +
                    "    not(AtomicInteger( get() == 1 ) and String( this == \"test_1\" )) \n" +
                    "then\n" +
                    "    System.out.println(\"R1\");" +
                    "end\n" +
                    "\n" +
                    "rule R2 when\n" +
                    "    AtomicInteger() \n" +
                    "    not(AtomicInteger( get() == 1 ) and String( this == \"test_1\" )) \n" +
                    "    String( this != \"test_2\" ) \n" +
                    "then\n" +
                    "    System.out.println(\"R2\");" +
                    "end\n";
    private final static String sharedSubnetworkExistsRule =
            "import " + AtomicInteger.class.getCanonicalName() + ";\n" +
                    "rule R1 when\n" +
                    "    AtomicInteger() \n" +
                    "    exists(AtomicInteger( get() == 1 ) and String( this == \"test_1\" )) \n" +
                    "then\n" +
                    "    System.out.println(\"R1\");" +
                    "end\n" +
                    "\n" +
                    "rule R2 when\n" +
                    "    AtomicInteger() \n" +
                    "    exists(AtomicInteger( get() == 1 ) and String( this == \"test_1\" )) \n" +
                    "    String( this != \"test_2\" ) \n" +
                    "then\n" +
                    "    System.out.println(\"R2\");" +
                    "end\n";

    @ParameterizedConcurrentInsertionsToSubnetworksTest
    public void testConcurrentInsertionsFewObjectsManyThreads(String drl) throws InterruptedException {
        testConcurrentInsertions(drl, 1, 1000, false, false);
    }

    @ParameterizedConcurrentInsertionsToSubnetworksTest
    public void testConcurrentInsertionsManyObjectsFewThreads(String drl) throws InterruptedException {
        testConcurrentInsertions(drl, 500, 4, false, false);
    }

    @ParameterizedConcurrentInsertionsToSubnetworksTest
    public void testConcurrentInsertionsManyObjectsSingleThread(String drl) throws InterruptedException {
        testConcurrentInsertions(drl, 1000, 1, false, false);
    }

    @ParameterizedConcurrentInsertionsToSubnetworksTest
    public void testConcurrentInsertionsNewSessionEachThread(String drl) throws InterruptedException {
        testConcurrentInsertions(drl, 10, 1000, true, false);
    }

    @ParameterizedConcurrentInsertionsToSubnetworksTest
    public void testConcurrentInsertionsNewSessionEachThreadUpdate(String drl) throws InterruptedException {
        testConcurrentInsertions(drl, 10, 1000, true, true);
    }

    protected Callable<Boolean> getTask(
            final int objectCount,
            final KieSession ksession,
            final boolean disposeSession,
            final boolean updateFacts) {
        return () -> {
            try {
                for (int j = 0; j < 10; j++) {
                    final FactHandle[] facts = new FactHandle[objectCount];
                    final FactHandle[] stringFacts = new FactHandle[objectCount];
                    for (int i = 0; i < objectCount; i++) {
                        facts[i] = ksession.insert(new AtomicInteger(i));
                        stringFacts[i] = ksession.insert("test_" + i);
                    }
                    if (updateFacts) {
                        for (int i = 0; i < objectCount; i++) {
                            ksession.update(facts[i], new AtomicInteger(-i));
                            ksession.update(stringFacts[i], "updated_test_" + i);
                        }
                    }
                    for (int i = 0; i < objectCount; i++) {
                        ksession.delete(facts[i]);
                        ksession.delete(stringFacts[i]);
                    }
                    ksession.fireAllRules();
                }
                return true;
            } catch (final Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (disposeSession) {
                    ksession.dispose();
                }
            }
        };
    }

    protected void testConcurrentInsertions(final String drl, final int objectCount, final int threadCount,
                                            final boolean newSessionForEachThread,
                                            final boolean updateFacts) throws InterruptedException {

        final KieBase kieBase = new KieHelper().addContent(drl, ResourceType.DRL).build();

        final ExecutorService executor = Executors.newFixedThreadPool(threadCount, r -> {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        KieSession ksession = null;
        try {
            final Callable[] tasks = new Callable[threadCount];
            if (newSessionForEachThread) {
                for (int i = 0; i < threadCount; i++) {
                    tasks[i] = getTask(objectCount, kieBase, updateFacts);
                }
            } else {
                ksession = kieBase.newKieSession();
                for (int i = 0; i < threadCount; i++) {
                    tasks[i] = getTask(objectCount, ksession, false, updateFacts);
                }
            }

            final CompletionService<Boolean> ecs = new ExecutorCompletionService<>(executor);
            for (final Callable task : tasks) {
                ecs.submit(task);
            }

            org.junit.jupiter.api.Assertions.assertTimeout(Duration.ofSeconds(10), () -> {
                int successCounter = 0;
                for (int i = 0; i < threadCount; i++) {
                    try {
                        if (ecs.take().get()) {
                            successCounter++;
                        }
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                Assertions.assertThat(successCounter).isEqualTo(threadCount);
            });
            if (ksession != null) {
                ksession.dispose();
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    private Callable<Boolean> getTask(final int objectCount, final KieBase kieBase, final boolean updateFacts) {
        return getTask(objectCount, kieBase.newKieSession(), true, updateFacts);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ParameterizedTest
    @MethodSource("parameters")
    public @interface ParameterizedConcurrentInsertionsToSubnetworksTest {

    }
}

