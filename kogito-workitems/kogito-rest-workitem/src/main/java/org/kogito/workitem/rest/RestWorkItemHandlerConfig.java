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
package org.kogito.workitem.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestWorkItemHandlerConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestWorkItemHandlerConfig.class);
    private static final String CONFIG_PREFIX = "kogito.processes.";

    private final Map<String, Map<String, String>> processTaskConfigs = new HashMap<>();

    
    public void registerProperty(String processId, String taskName, String propertyName, String propertyValue) {
        String key = buildKey(processId, taskName);
        processTaskConfigs.computeIfAbsent(key, k -> new HashMap<>()).put(propertyName, propertyValue);
        logger.debug("Registered REST config: {} -> {}={}", key, propertyName, propertyValue);
    }

   
    public Map<String, String> getConfig(String processId, String taskName) {
        String key = buildKey(processId, taskName);
        return processTaskConfigs.getOrDefault(key, new HashMap<>());
    }

   
    public Optional<String> getProperty(String processId, String taskName, String propertyName) {
        return Optional.ofNullable(getConfig(processId, taskName).get(propertyName));
    }

  
    public boolean parseAndRegister(String fullPropertyKey, String propertyValue) {
        if (!fullPropertyKey.startsWith(CONFIG_PREFIX)) {
            return false;
        }

        String remainder = fullPropertyKey.substring(CONFIG_PREFIX.length());
        String[] parts = remainder.split("\\.", 3);

        if (parts.length < 3) {
            logger.warn("Invalid REST config property format: {}. Expected: kogito.processes.{{processId}}.{{taskName}}.{{property}}",
                    fullPropertyKey);
            return false;
        }

        String processId = parts[0];
        String taskName = parts[1];
        String propertyName = parts[2];

        registerProperty(processId, taskName, propertyName, propertyValue);
        return true;
    }

   
    public void clear() {
        processTaskConfigs.clear();
    }

    
    public Set<String> getAllProcessIds() {
        return processTaskConfigs.keySet().stream()
                .map(key -> key.split("\\.")[0])
                .collect(Collectors.toSet());
    }

 
    public Set<String> getAllProcessTaskKeys() {
        return processTaskConfigs.keySet();
    }

    private String buildKey(String processId, String taskName) {
        return processId + "." + taskName;
    }


    public static String getConfigPrefix() {
        return CONFIG_PREFIX;
    }
}


