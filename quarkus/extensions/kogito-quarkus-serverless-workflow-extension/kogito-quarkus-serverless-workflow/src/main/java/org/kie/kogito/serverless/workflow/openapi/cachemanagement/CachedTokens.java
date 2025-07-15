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
package org.kie.kogito.serverless.workflow.openapi.cachemanagement;

/**
 * Data structure to hold both access and refresh tokens with expiration information.
 */
public class CachedTokens {
    private final String accessToken;
    private final String refreshToken;
    private final long expirationTime; // Unix timestamp

    public CachedTokens(String accessToken, String refreshToken, long expirationTime) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTime = expirationTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Checks if the token is expired right now.
     * 
     * @return true if the token is expired
     */
    public boolean isExpiredNow() {
        return System.currentTimeMillis() / 1000 >= expirationTime;
    }

    /**
     * Checks if the token is expiring soon based on the provided buffer.
     * 
     * @param bufferSeconds Number of seconds before expiration to consider "expiring soon"
     * @return true if the token is expiring within the buffer time
     */
    public boolean isExpiringSoon(long bufferSeconds) {
        return System.currentTimeMillis() / 1000 >= (expirationTime - bufferSeconds);
    }

}