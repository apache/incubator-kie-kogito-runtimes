/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.services.uow;

import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.WorkUnit;

/**
 * The simplest version of unit of work (and one used when no other is configured)
 * that simply pass through the work it intercepts. It has no operation methods for
 * life cycle methods like start, end and abort.
 *
 */
public class PassThroughUnitOfWork implements UnitOfWork {

    @Override
    public void start() {
        // no-op

    }

    @Override
    public void end() {
        // no-op

    }

    @Override
    public void abort() {
        // no-op

    }

    @Override
    public void intercept(WorkUnit work) {
        work.perform();
    }

}
