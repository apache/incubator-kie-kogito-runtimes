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
package org.kie.kogito.services.uow;

import java.util.function.Supplier;

import org.kie.kogito.process.ProcessInstanceExecutionException;
import org.kie.kogito.services.context.ProcessInstanceContext;
import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.UnitOfWorkManager;

public class UnitOfWorkExecutor {

    private UnitOfWorkExecutor() {

    }

    public static <T> T executeInUnitOfWork(UnitOfWorkManager uowManager, Supplier<T> supplier) {
        T result = null;
        UnitOfWork uow = uowManager.newUnitOfWork();

        try {
            uow.start();

            result = supplier.get();
            uow.end();

            return result;
        } catch (ProcessInstanceExecutionException e) {
            uow.end();
            throw e;
        } catch (Exception e) {
            uow.abort();
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Executes the given supplier within a process-instance-aware unit of work.
     * This method ensures that the process instance context is properly managed
     * throughout the unit of work lifecycle.
     *
     * @param uowManager the unit of work manager
     * @param processInstanceId the process instance ID to use for context
     * @param supplier the supplier to execute
     * @param <T> the return type
     * @return the result of the supplier
     */
    public static <T> T executeInUnitOfWork(UnitOfWorkManager uowManager, String processInstanceId, Supplier<T> supplier) {
        T result = null;
        UnitOfWork baseUow = uowManager.newUnitOfWork();
        UnitOfWork uow = new ProcessInstanceAwareUnitOfWork(baseUow, processInstanceId);

        try {
            uow.start();

            result = supplier.get();
            uow.end();

            return result;
        } catch (ProcessInstanceExecutionException e) {
            uow.end();
            throw e;
        } catch (Exception e) {
            uow.abort();
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Executes the given supplier within a unit of work with automatic process instance context detection.
     * If a process instance context is already set, it will be used for the unit of work.
     * Otherwise, a standard unit of work will be created.
     *
     * @param uowManager the unit of work manager
     * @param supplier the supplier to execute
     * @param <T> the return type
     * @return the result of the supplier
     */
    public static <T> T executeInProcessAwareUnitOfWork(UnitOfWorkManager uowManager, Supplier<T> supplier) {
        String processInstanceId = ProcessInstanceContext.getProcessInstanceId();

        if (processInstanceId != null) {
            return executeInUnitOfWork(uowManager, processInstanceId, supplier);
        } else {
            return executeInUnitOfWork(uowManager, supplier);
        }
    }
}
