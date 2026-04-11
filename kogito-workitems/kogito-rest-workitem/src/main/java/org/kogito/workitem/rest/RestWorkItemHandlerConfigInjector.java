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

import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestWorkItemHandlerConfigInjector {

    private static final Logger logger = LoggerFactory.getLogger(RestWorkItemHandlerConfigInjector.class);

     
    private static final Map<String, String> PROPERTY_MAPPINGS = new HashMap<>();

    static {
        
        PROPERTY_MAPPINGS.put("accessTokenStrategy", "AccessTokenAcquisitionStrategy");
        PROPERTY_MAPPINGS.put("access_token_strategy", "AccessTokenAcquisitionStrategy");
        PROPERTY_MAPPINGS.put("authStrategy", "AccessTokenAcquisitionStrategy");

        
        PROPERTY_MAPPINGS.put("access_token", "propagateToken");
        PROPERTY_MAPPINGS.put("accessToken", "propagateToken");
        PROPERTY_MAPPINGS.put("bearer_token", "propagateToken");
        PROPERTY_MAPPINGS.put("bearerToken", "propagateToken");
        PROPERTY_MAPPINGS.put("token", "propagateToken");

        
        PROPERTY_MAPPINGS.put("clientId", "clientId");
        PROPERTY_MAPPINGS.put("client_id", "clientId");
        PROPERTY_MAPPINGS.put("clientSecret", "clientSecret");
        PROPERTY_MAPPINGS.put("client_secret", "clientSecret");
        PROPERTY_MAPPINGS.put("tokenUrl", "tokenUrl");
        PROPERTY_MAPPINGS.put("token_url", "tokenUrl");
        PROPERTY_MAPPINGS.put("refreshUrl", "refreshUrl");
        PROPERTY_MAPPINGS.put("refresh_url", "refreshUrl");

        
        PROPERTY_MAPPINGS.put("username", RestWorkItemHandler.USER);
        PROPERTY_MAPPINGS.put("password", RestWorkItemHandler.PASSWORD);

        
        PROPERTY_MAPPINGS.put("apiKey", "apiKey");
        PROPERTY_MAPPINGS.put("api_key", "apiKey");

        
        PROPERTY_MAPPINGS.put("host", RestWorkItemHandler.HOST);
        PROPERTY_MAPPINGS.put("port", RestWorkItemHandler.PORT);
        PROPERTY_MAPPINGS.put("protocol", RestWorkItemHandler.PROTOCOL);
        PROPERTY_MAPPINGS.put("url", RestWorkItemHandler.URL);
        PROPERTY_MAPPINGS.put("endpoint", RestWorkItemHandler.URL);
        PROPERTY_MAPPINGS.put("method", RestWorkItemHandler.METHOD);

        
        PROPERTY_MAPPINGS.put("requestTimeout", RestWorkItemHandler.REQUEST_TIMEOUT_IN_MILLIS);
        PROPERTY_MAPPINGS.put("request_timeout", RestWorkItemHandler.REQUEST_TIMEOUT_IN_MILLIS);
        PROPERTY_MAPPINGS.put("timeout", RestWorkItemHandler.REQUEST_TIMEOUT_IN_MILLIS);
    }

    private final RestWorkItemHandlerConfig config;

    public RestWorkItemHandlerConfigInjector(RestWorkItemHandlerConfig config) {
        this.config = config;
    }

    
    public void injectConfig(KogitoWorkItem workItem, Map<String, Object> parameters, String processId, String taskName) {
        if (processId == null || taskName == null) {
            logger.debug("Process ID or task name is null, skipping config injection");
            return;
        }

        logger.debug("Looking up configuration for process '{}' and task '{}'", processId, taskName);
        Map<String, String> taskConfig = config.getConfig(processId, taskName);
        if (taskConfig.isEmpty()) {
            logger.debug("No configuration found for process '{}' and task '{}'. Available configurations: {}",
                    processId, taskName, config.getAllProcessTaskKeys());
            return;
        }

        logger.info("Injecting configuration for process '{}' and task '{}': {}", processId, taskName, taskConfig.keySet());

        for (Map.Entry<String, String> entry : taskConfig.entrySet()) {
            String configKey = entry.getKey();
            String configValue = entry.getValue();

            
            String parameterName = PROPERTY_MAPPINGS.getOrDefault(configKey, configKey);

            
            Object existingValue = parameters.get(parameterName);

           boolean shouldInject = existingValue == null ||
                    (existingValue instanceof String && configKey.equals(existingValue));

            if (shouldInject) {
                Object convertedValue = convertValue(parameterName, configValue);
                parameters.put(parameterName, convertedValue);
                logger.debug("Injected config property: {} -> {} = {} (replaced: {})",
                        configKey, parameterName, convertedValue, existingValue);
            } else {
                logger.debug("Skipping config property '{}' as it's already set in parameters with value: {}",
                        parameterName, existingValue);
            }
        }
    }

   
    private Object convertValue(String parameterName, String value) {
        if (value == null) {
            return null;
        }

        
        if (RestWorkItemHandler.PORT.equals(parameterName)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Failed to convert port value '{}' to integer, using as string", value);
                return value;
            }
        }

        
        if (RestWorkItemHandler.REQUEST_TIMEOUT_IN_MILLIS.equals(parameterName)) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("Failed to convert timeout value '{}' to long, using as string", value);
                return value;
            }
        }

      
        if (isListBasedProperty(parameterName) && value.contains(",")) {
            return parseCommaSeparatedValue(value);
        }

      
        return value;
    }

   
    private boolean isListBasedProperty(String parameterName) {
        
        return "AccessTokenAcquisitionStrategy".equals(parameterName) ||
                "authStrategy".equals(parameterName) ||
                "authenticationStrategy".equals(parameterName);
    }

   
    private java.util.List<String> parseCommaSeparatedValue(String value) {
        String[] parts = value.split(",");
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        logger.debug("Parsed comma-separated value '{}' into list: {}", value, result);
        return result;
    }

  
    public static void addPropertyMapping(String configKey, String parameterName) {
        PROPERTY_MAPPINGS.put(configKey, parameterName);
    }

   
    public static Map<String, String> getPropertyMappings() {
        return new HashMap<>(PROPERTY_MAPPINGS);
    }
}


