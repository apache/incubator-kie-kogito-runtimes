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
package org.kie.kogito;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides direct information about addons installed and available
 * within the running service.
 *
 */
public class Addons {
    /**
     * Default empty addons instance
     */
    public static final Addons EMTPY = new Addons(Collections.emptySet());

    private final Set<String> availableAddons;

    public Addons(Set<String> availableAddons) {
        this.availableAddons = availableAddons;
    }

    /**
     * Returns all available addons
     * 
     * @return returns addons
     */
    public Set<String> availableAddons() {
        return availableAddons;
    }

    @Override
    public String toString() {
        return availableAddons.stream().collect(Collectors.joining(","));
    }
}
