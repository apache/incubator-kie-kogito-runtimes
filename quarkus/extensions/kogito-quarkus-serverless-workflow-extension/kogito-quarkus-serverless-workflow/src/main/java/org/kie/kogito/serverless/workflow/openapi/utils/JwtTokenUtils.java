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
package org.kie.kogito.serverless.workflow.openapi.utils;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JWT token parsing operations.
 */
public final class JwtTokenUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtils.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JwtTokenUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Parses the expiration time from a JWT access token.
     * 
     * @param accessToken The JWT access token
     * @return The expiration time as Unix timestamp, or default expiration if parsing fails
     */
    public static long parseTokenExpiration(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length != 3) {
                LOGGER.warn("Invalid JWT token format while parsing token expiration, will use default expiration of 1 hour");
                return System.currentTimeMillis() / 1000 + 3600; // Default to 1 hour
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode json = MAPPER.readTree(payload);

            if (json.has("exp")) {
                return json.get("exp").asLong();
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to parse token expiration: {}", e.getMessage());
        }

        // Default expiration if parsing fails
        return System.currentTimeMillis() / 1000 + 3600; // 1 hour from now
    }
}