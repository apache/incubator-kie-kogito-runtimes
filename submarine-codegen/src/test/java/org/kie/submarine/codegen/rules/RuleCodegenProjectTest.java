/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.submarine.codegen.rules;

import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuleCodegenProjectTest {

    @Test
    public void withModuleGenerator() {
        final RuleCodegenProject project = new RuleCodegenProject(null, null);
        assertThat(project.withModuleGenerator(null)).isSameAs(project);
    }

    @Test
    public void writeProjectOutput() {
        final InternalKieModule kieModule = mock(InternalKieModule.class);
        when(kieModule.getReleaseId()).thenReturn(new ReleaseIdImpl("testGroupId", "testArtifactId", "1.0.0"));
        final RuleCodegenProject project = new RuleCodegenProject(kieModule, null);
        project.writeProjectOutput(new MemoryFileSystem(), new ResultsImpl());


        // TODO
    }

    @Test
    public void writeProjectOutputWithoutModuleGenerator() {
        final InternalKieModule kieModule = mock(InternalKieModule.class);
        when(kieModule.getReleaseId()).thenReturn(new ReleaseIdImpl("testGroupId", "testArtifactId", "1.0.0"));
        final RuleCodegenProject project = new RuleCodegenProject(kieModule, null);
        assertThatThrownBy(() -> project.writeProjectOutput(new MemoryFileSystem(), new ResultsImpl())).isInstanceOf(IllegalStateException.class);
    }
}