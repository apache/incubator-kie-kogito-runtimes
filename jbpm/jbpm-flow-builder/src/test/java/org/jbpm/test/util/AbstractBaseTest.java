/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.test.util;

import java.util.Arrays;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.jbpm.integrationtests.JbpmSerializationHelper;
import org.jbpm.process.instance.impl.util.LoggingPrintStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.runtime.conf.ForceEagerActivationOption;

public abstract class AbstractBaseTest {

    protected KnowledgeBuilderImpl builder;

    @BeforeEach
    public void before() {
        builder = new KnowledgeBuilderImpl();
    }

    public KieSession createKieSession(KiePackage... pkg) {
        try {
            return createKieSession(false, pkg);
        } catch (Exception e) {
            throw new RuntimeException("There's no reason for an exception to be thrown here (because the kbase is not being serialized)!", e);
        }
    }

    public KieSession createKieSession(boolean serializeKbase, KiePackage... pkg) throws Exception {
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(Arrays.asList(pkg));
        if (serializeKbase) {
            kbase = JbpmSerializationHelper.serializeObject(kbase);
        }

        KieSessionConfiguration conf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        conf.setOption(ForceEagerActivationOption.YES);
        return kbase.newKieSession(conf, null);
    }

    @BeforeAll
    public static void configure() {
        LoggingPrintStream.interceptSysOutSysErr();
    }

    @AfterAll
    public static void reset() {
        LoggingPrintStream.resetInterceptSysOutSysErr();
    }
}
