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

package org.kie.kogito.legacy.rules.impl;

import java.util.Collection;
import java.util.Set;

import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.KieSessionsPool;
import org.kie.api.runtime.StatelessKieSession;

public class KieBaseImpl implements org.kie.api.KieBase {

    private final KieBase delegate;

    public KieBaseImpl(KieBase delegate) {
        this.delegate = delegate;
    }

    public Collection<KiePackage> getKiePackages() {
        return delegate.getKiePackages();
    }

    public KiePackage getKiePackage(String s) {
        return delegate.getKiePackage(s);
    }

    public void removeKiePackage(String s) {
        delegate.removeKiePackage(s);
    }

    public Rule getRule(String s, String s1) {
        return delegate.getRule(s, s1);
    }

    public void removeRule(String s, String s1) {
        delegate.removeRule(s, s1);
    }

    public Query getQuery(String s, String s1) {
        return delegate.getQuery(s, s1);
    }

    public void removeQuery(String s, String s1) {
        delegate.removeQuery(s, s1);
    }

    public void removeFunction(String s, String s1) {
        delegate.removeFunction(s, s1);
    }

    public FactType getFactType(String s, String s1) {
        return delegate.getFactType(s, s1);
    }

    public Process getProcess(String s) {
        throw new UnsupportedOperationException();
    }

    public void removeProcess(String s) {
        throw new UnsupportedOperationException();
    }

    public Collection<Process> getProcesses() {
        throw new UnsupportedOperationException();
    }

    public KieSession newKieSession(KieSessionConfiguration kieSessionConfiguration, Environment environment) {
        return new KieSessionImpl(delegate.newKieSession(kieSessionConfiguration, environment));
    }

    public KieSession newKieSession() {
        return new KieSessionImpl(delegate.newKieSession());
    }

    public KieSessionsPool newKieSessionsPool(int i) {
        return delegate.newKieSessionsPool(i);
    }

    public Collection<? extends KieSession> getKieSessions() {
        return delegate.getKieSessions();
    }

    public StatelessKieSession newStatelessKieSession(KieSessionConfiguration kieSessionConfiguration) {
        return delegate.newStatelessKieSession(kieSessionConfiguration);
    }

    public StatelessKieSession newStatelessKieSession() {
        return delegate.newStatelessKieSession();
    }

    public Set<String> getEntryPointIds() {
        return delegate.getEntryPointIds();
    }

    public void addEventListener(KieBaseEventListener kieBaseEventListener) {
        delegate.addEventListener(kieBaseEventListener);
    }

    public void removeEventListener(KieBaseEventListener kieBaseEventListener) {
        delegate.removeEventListener(kieBaseEventListener);
    }

    public Collection<KieBaseEventListener> getKieBaseEventListeners() {
        return delegate.getKieBaseEventListeners();
    }
}
