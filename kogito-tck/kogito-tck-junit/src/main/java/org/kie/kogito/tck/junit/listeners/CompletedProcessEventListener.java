/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.tck.junit.listeners;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;

/**
 * Simple listener for watching process flow
 */
public class CompletedProcessEventListener extends DefaultKogitoProcessEventListener {

    private CountDownLatch latch;

    public CompletedProcessEventListener() {
        latch = new CountDownLatch(1);
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
     
        super.beforeProcessStarted(event);
    }
    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        latch.countDown();
    }

    public void waitForCompletion(long seconds) throws InterruptedException {
        latch.await(seconds, TimeUnit.SECONDS);
    }

    public void waitForCompletion() throws InterruptedException {
        latch.await();
    }
}