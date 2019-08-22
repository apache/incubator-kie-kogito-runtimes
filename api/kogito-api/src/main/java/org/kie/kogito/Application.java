/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.kie.kogito.process.Processes;
import org.kie.kogito.rules.RuleUnits;
import org.kie.kogito.uow.UnitOfWorkManager;

/**
 * Entry point for accessing business automation components
 * such as processes, rules, decisions, etc.
 * <p>
 * It should be considered as singleton kind of object that can be safely
 * used across entire application.
 */
public interface Application {

    static Application create() {
        try {
            Class<Application> appClass = (Class<Application>) Class.forName("org.kie.kogito.$ApplicationStub$");
            Method create = appClass.getMethod("create");
            return (Application) create.invoke(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Returns configuration of the application
     * @return current configuration
     */
    Config config();

    /**
     * Returns processes found in the application otherwise null
     * @return processes information or null of non found
     */
    default Processes processes() {
        return null;
    }

    /**
     * Returns rule units found in the application otherwise null
     * @return rule unit information or null if not found
     */
    default RuleUnits ruleUnits() {
        return null;
    }
    
    /**
     * Returns unit of work manager that allows to control execution within the application
     * @return non null unit of work manager
     */
    UnitOfWorkManager unitOfWorkManager();
}
