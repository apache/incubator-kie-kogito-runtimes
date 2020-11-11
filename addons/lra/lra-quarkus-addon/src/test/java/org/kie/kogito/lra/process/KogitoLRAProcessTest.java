/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.lra.process;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;

import io.narayana.lra.client.NarayanaLRAClient;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.lra.KogitoLRA;
import org.kie.kogito.lra.model.LRAContext;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@QuarkusTest
class KogitoLRAProcessTest {

    static final UriBuilder URI_TEMPLATE = UriBuilder.fromPath("http://localhost:8080").path("lra-process").path(KogitoLRA.LRA_RESOURCE).path("{action}");
    final URI lraUri = URI.create("org.example/lra-process/0001");

    @Inject
    @Named("lra-process")
    Process<LRAProcessModel> process;

    NarayanaLRAClient lraClient;

    @BeforeEach
    void init() {
        lraClient = Mockito.mock(NarayanaLRAClient.class, KogitoLRA.BEAN_NAME);
        QuarkusMock.installMockForType(lraClient, NarayanaLRAClient.class);
        when(lraClient.startLRA(null, "lra-process", 0L, ChronoUnit.SECONDS)).thenReturn(lraUri);
    }

    @Test
    void testStartLRA() {
        Map<String, Object> params = new HashMap<>();
        params.put("message", "hello");
        params.put("history", new ArrayList<>());
        LRAProcessModel model = this.process.createModel();
        model.fromMap(params);

        ProcessInstance<LRAProcessModel> instance = this.process.createInstance(model);
        instance.start();
        verifyStartLRA();

        completeTask(instance);

        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Mockito.verify(lraClient, times(1)).closeLRA(lraUri);
    }

    @Test
    void testJoinLRA() {
        final URI recoveryUri = URI.create("recovery/org.example.com/lra-rocess/0001");
        when(lraClient.joinLRA(eq(lraUri), anyLong(), any(URI.class), any(URI.class), any(URI.class), any(URI.class), any(URI.class), nullable(URI.class), nullable(String.class)))
                .thenReturn(recoveryUri);
        Map<String, Object> params = new HashMap<>();
        params.put("history", new ArrayList<>());
        LRAProcessModel model = this.process.createModel();
        model.fromMap(params);
        //Added by code generation
        LRAContext context = new LRAContext().setUri(lraUri).setBasePath(UriBuilder.fromPath("http://localhost:8080").path(process.id()).build());
        ProcessInstance<LRAProcessModel> instance = this.process.createInstance(model);
        ((AbstractProcessInstance<LRAProcessModel>) instance).internalGetProcessInstance().setMetaData(KogitoLRA.LRA_CONTEXT, context);
        instance.start();
        verifyJoinLRA();

        completeTask(instance);

        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        Mockito.verify(lraClient, times(0)).closeLRA(lraUri);
    }


    @Test
    void testCancelLRAWithAbortedProcess() {
        Map<String, Object> params = new HashMap<>();
        params.put("message", "hello");
        params.put("history", new ArrayList<>());

        LRAProcessModel model = this.process.createModel();
        model.fromMap(params);

        ProcessInstance<LRAProcessModel> instance = this.process.createInstance(model);
        instance.start();
        verifyStartLRA();

        instance.abort();

        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ABORTED);
        Mockito.verify(lraClient, times(1)).cancelLRA(lraUri);
    }

    @Test
    void testCancelLRAWithErroredProcess() {
        Map<String, Object> params = new HashMap<>();
        params.put("message", "hello");
        params.put("history", new ArrayList<>());

        LRAProcessModel model = this.process.createModel();
        model.fromMap(params);

        ProcessInstance<LRAProcessModel> instance = this.process.createInstance(model);
        instance.start();
        verifyStartLRA();

        WorkItem humanTask = instance.workItems().get(0);
        Map<String, Object> variables = new HashMap<>();
        variables.put("message", "fail");
        instance.completeWorkItem(humanTask.getId(), variables);

        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ERROR);
        Mockito.verify(lraClient, times(0)).cancelLRA(lraUri);

        instance.abort();
        assertThat(instance.status()).isEqualTo(ProcessInstance.STATE_ABORTED);
        Mockito.verify(lraClient, times(1)).cancelLRA(lraUri);
    }

    private void verifyStartLRA() {
        Mockito.verify(lraClient, times(1)).startLRA(null, "lra-process", 0L, ChronoUnit.SECONDS);
    }

    private void verifyJoinLRA() {
        // The process instance id is not validated because it's not possible to intercept the step
        // between the id generation and the LRA join. This will be tested in the IT
        Mockito.verify(lraClient, times(1)).joinLRA(lraUri, LRAProcess.LRA_TIMEOUT,
                URI_TEMPLATE.build("compensate"),
                URI_TEMPLATE.build("complete"),
                URI_TEMPLATE.build("forget"),
                URI_TEMPLATE.build("leave"),
                URI_TEMPLATE.build("after"),
                null,
                null);
    }

    private void completeTask(ProcessInstance<LRAProcessModel> instance) {
        WorkItem humanTask = instance.workItems().get(0);
        Map<String, Object> variables = new HashMap<>();
        variables.put("message", "hello");
        instance.completeWorkItem(humanTask.getId(), variables);

        LRAProcessModel model = instance.variables();
        List<String> history = (List<String>) model.get("history");
        assertThat(history.size()).isEqualTo(1);
        assertThat(history).contains("hello");
    }
}
