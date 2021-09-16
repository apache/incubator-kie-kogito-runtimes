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
package org.kie.kogito.quarkus.drools;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.time.SessionPseudoClock;
import org.kie.kogito.legacy.rules.KieRuntimeBuilder;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class RuntimeIT {

    @Inject
    KieRuntimeBuilder runtimeBuilder;

    @Test
    public void testRuleEvaluation() {
        KieSession ksession = runtimeBuilder.newKieSession("canDrinkKS");

        Result result = new Result();
        ksession.insert(result);
        ksession.insert(new Person("Mark", 17));
        ksession.fireAllRules();

        assertEquals("Mark can NOT drink", result.toString());
    }

    @Test
    public void testCepEvaluation() {
        KieSession ksession = runtimeBuilder.newKieSession("cepKS");

        SessionPseudoClock clock = ksession.getSessionClock();

        ksession.insert(new StockTick("DROO"));
        clock.advanceTime(6, TimeUnit.SECONDS);
        ksession.insert(new StockTick("ACME"));

        assertEquals(1, ksession.fireAllRules());

        clock.advanceTime(4, TimeUnit.SECONDS);
        ksession.insert(new StockTick("ACME"));

        assertEquals(0, ksession.fireAllRules());
    }

    @Test
    public void testFireUntiHalt() {
        KieSession ksession = runtimeBuilder.newKieSession("probeKS");

        SessionPseudoClock clock = ksession.getSessionClock();

        new Thread(ksession::fireUntilHalt).start();
        final ProbeCounter pc = new ProbeCounter();

        ksession.insert(pc);
        clock.advanceTime(1, TimeUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            ksession.insert(new ProbeEvent(i));
            clock.advanceTime(1, TimeUnit.SECONDS);
        }

        synchronized (pc) {
            if (pc.getTotal() < 10) {
                try {
                    pc.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        assertEquals(10, pc.getTotal());
    }
}
