/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.process.impl.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInstanceAtomicLockStrategy implements ProcessInstanceLockStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessInstanceAtomicLockStrategy.class);

    private static ProcessInstanceAtomicLockStrategy INSTANCE;

    private Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public <T> T executeOperation(String processInstanceId, WorkflowAtomicExecutor<T> executor) {
        try {
            ReentrantLock lock = locks.computeIfAbsent(processInstanceId, pi -> new ReentrantLock());
            boolean alreadyAdquired = lock.isHeldByCurrentThread();
            if (!alreadyAdquired) {
                LOG.debug("about to adquire lock for {}", processInstanceId);
            }
            lock.lock();
            if (!alreadyAdquired) {
                LOG.debug("lock adquired for {}", processInstanceId);
            }
            return executor.execute();
        } finally {

            ReentrantLock lock = locks.get(processInstanceId);
            lock.unlock();
            if (!lock.isHeldByCurrentThread()) {
                LOG.debug("lock realeased for {}", processInstanceId);
            }
        }

    }

    public static synchronized ProcessInstanceLockStrategy instance() {
        if (INSTANCE == null) {
            INSTANCE = new ProcessInstanceAtomicLockStrategy();
        }
        return INSTANCE;
    }

}
