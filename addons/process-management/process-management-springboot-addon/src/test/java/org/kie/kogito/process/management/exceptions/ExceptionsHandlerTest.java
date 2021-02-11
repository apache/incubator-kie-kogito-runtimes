/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.process.management.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.process.NodeInstanceNotFoundException;
import org.kie.kogito.process.ProcessInstanceDuplicatedException;
import org.kie.kogito.process.ProcessInstanceExecutionException;
import org.kie.kogito.process.ProcessInstanceNotFoundException;
import org.kie.kogito.process.VariableViolationException;
import org.kie.kogito.process.workitem.InvalidLifeCyclePhaseException;
import org.kie.kogito.process.workitem.InvalidTransitionException;
import org.kie.kogito.process.workitem.NotAuthorizedException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExceptionsHandlerTest {

    private ExceptionsHandler tested;

    @Mock
    private Object body;

    @BeforeEach
    void setUp() {
        tested = spy(new ExceptionsHandler());
    }

    @Test
    void testBadRequest() {
        ResponseEntity responseEntity = tested.badRequest(body);
        assertResponse(responseEntity, HttpStatus.BAD_REQUEST);
    }

    private void assertResponse(ResponseEntity responseEntity, HttpStatus status) {
        assertThat(responseEntity.getStatusCode()).isEqualTo(status);
        assertThat(responseEntity.getBody()).isEqualTo(body);
    }

    @Test
    void testConflict() {
        ResponseEntity responseEntity = tested.conflict(body);
        assertResponse(responseEntity, HttpStatus.CONFLICT);
    }

    @Test
    void testIternalError() {
        ResponseEntity responseEntity = tested.internalError(body);
        assertResponse(responseEntity, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testNotFound() {
        ResponseEntity responseEntity = tested.badRequest(body);
        assertResponse(responseEntity, HttpStatus.BAD_REQUEST);
    }

    @Test
    void testForbidden() {
        ResponseEntity responseEntity = tested.forbidden(body);
        assertResponse(responseEntity, HttpStatus.FORBIDDEN);
    }

    @Test
    void testInvalidLifeCyclePhaseException(@Mock InvalidLifeCyclePhaseException exception) {
        tested.toResponse(exception);
        verify(tested).mapException(exception);
    }

    @Test
    void testInvalidTransitionException(@Mock InvalidTransitionException exception) {
        tested.toResponse(exception);
        verify(tested).mapException(exception);
    }

    @Test
    void testNodeInstanceNotFoundException(@Mock NodeInstanceNotFoundException exception) {
        tested.toResponse(exception);
        verify(tested).mapException(exception);
    }

    @Test
    void testNotAuthorizedException(@Mock NotAuthorizedException exception) {
        tested.toResponse(exception);
        verify(tested).mapException(exception);
    }

    @Test
    void testProcessInstanceDuplicatedException(@Mock ProcessInstanceDuplicatedException exception) {
        tested.toResponse(exception);
        verify(tested).mapException(exception);
    }

    @Test
    void testProcessInstanceExecutionException(@Mock ProcessInstanceExecutionException exception) {
        tested.toResponse(exception);
        verify(tested).mapException(exception);
    }

    @Test
    void testProcessInstanceNotFoundException(@Mock ProcessInstanceNotFoundException exception) {
        tested.toResponse(exception);
        verify(tested).mapException(exception);
    }

    @Test
    void testVariableViolationException(@Mock VariableViolationException exception) {
        tested.toResponse(exception);
        verify(tested).mapException(exception);
    }
}