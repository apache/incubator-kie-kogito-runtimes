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
package org.kie.kogito.serverless.workflow.openapi.persistence.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Simple POJO representing a token cache record for JDBC persistence.
 * Uses composite primary key of processInstanceId and authName.
 * Follows the same pattern as other JDBC entities in the codebase.
 */
public class TokenCacheRecord {

    private String processInstanceId;
    private String authName;
    private String accessToken;
    private String refreshToken;
    private Long expirationTime;
    private Instant createdAt;
    private Instant updatedAt;

    public TokenCacheRecord() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public TokenCacheRecord(String processInstanceId, String authName,
            String accessToken, String refreshToken, Long expirationTime) {
        this();
        this.processInstanceId = processInstanceId;
        this.authName = authName;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTime = expirationTime;
    }

    // Getters and setters

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void updateTimestamp() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TokenCacheRecord that = (TokenCacheRecord) o;
        return Objects.equals(processInstanceId, that.processInstanceId) &&
                Objects.equals(authName, that.authName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processInstanceId, authName);
    }
}