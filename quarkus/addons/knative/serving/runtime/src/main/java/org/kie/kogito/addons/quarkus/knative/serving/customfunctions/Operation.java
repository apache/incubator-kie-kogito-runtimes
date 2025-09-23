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
package org.kie.kogito.addons.quarkus.knative.serving.customfunctions;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.http.HttpMethod;

public final class Operation {

    private static final Set<HttpMethod> SUPPORTED_METHODS = Set.of(HttpMethod.POST, HttpMethod.GET);

    private static final HttpMethod DEFAULT_HTTP_METHOD = HttpMethod.POST;

    static final String CLOUD_EVENT_PARAMETER_NAME = "asCloudEvent";

    static final String PATH_PARAMETER_NAME = "path";

    static final String METHOD_PARAMETER_NAME = "method";

    static final String RETURN_HEADERS_PARAMETER_NAME = "returnHeaders";

    static final String RETURN_STATUS_CODE_PARAMETER_NAME = "returnStatusCode";

    static final String FAIL_ON_STATUS_ERROR_PARAMETER_NAME = "failOnStatusError";

    private final String service;

    private final String path;

    private final boolean isCloudEvent;

    private final HttpMethod httpMethod;

    private final boolean returnHeaders;

    private final boolean returnStatusCode;

    private final boolean failOnStatusError;

    private Operation(Builder builder) {
        this.service = Objects.requireNonNull(builder.service);
        this.path = builder.path != null ? builder.path : "/";
        this.isCloudEvent = builder.isCloudEvent;
        this.httpMethod = builder.httpMethod;
        this.returnHeaders = builder.returnHeaders;
        this.returnStatusCode = builder.returnStatusCode;
        this.failOnStatusError = builder.failOnStatusError;
        validate(this);
    }

    private static void validate(Operation operation) {
        if (!SUPPORTED_METHODS.contains(operation.getHttpMethod())) {
            throw new UnsupportedOperationException(
                    MessageFormat.format("Knative custom function doesn''t support the {0} HTTP method. Supported methods are: {1}.", operation.getHttpMethod(), SUPPORTED_METHODS));
        }

        if (operation.isCloudEvent() && !operation.getHttpMethod().equals(HttpMethod.POST)) {
            throw new UnsupportedOperationException(MessageFormat.format("Knative custom function can only send CloudEvents through POST method. Method used: {0}", operation.getHttpMethod()));
        }
    }

    public String getService() {
        return service;
    }

    public String getPath() {
        return path;
    }

    public boolean isCloudEvent() {
        return isCloudEvent;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public boolean returnHeaders() {
        return returnHeaders;
    }

    public boolean returnStatusCode() {
        return returnStatusCode;
    }

    public boolean failOnStatusError() {
        return failOnStatusError;
    }

    public static Operation parse(String value) {
        String[] parts = value.split("\\?", 2);

        String[] query = parts.length > 1 ? parts[1].split("&") : new String[0];
        Map<String, String> params = new HashMap<>();
        for (String param : query) {
            String[] pair = param.split("=", 2);
            params.put(pair[0], pair.length > 1 ? pair[1] : "");
        }

        return builder()
                .withService(parts[0])
                .withPath(params.get(PATH_PARAMETER_NAME))
                .withIsCloudEvent(Boolean.parseBoolean(params.get(CLOUD_EVENT_PARAMETER_NAME)))
                .withMethod(HttpMethod.valueOf(params.getOrDefault(METHOD_PARAMETER_NAME, DEFAULT_HTTP_METHOD.name()).toUpperCase()))
                .withReturnHeaders(Boolean.parseBoolean(params.get(RETURN_HEADERS_PARAMETER_NAME)))
                .withReturnStatusCode(Boolean.parseBoolean(params.get(RETURN_STATUS_CODE_PARAMETER_NAME)))
                .withFailOnStatusError(Boolean.parseBoolean(params.getOrDefault(FAIL_ON_STATUS_ERROR_PARAMETER_NAME, "true")))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Operation operation = (Operation) o;
        return isCloudEvent == operation.isCloudEvent
                && returnHeaders == operation.returnHeaders
                && returnStatusCode == operation.returnStatusCode
                && failOnStatusError == operation.failOnStatusError
                && Objects.equals(service, operation.service)
                && Objects.equals(path, operation.path)
                && Objects.equals(httpMethod, operation.httpMethod);
    }

    @Override
    public String toString() {
        return "Operation{" +
                "service='" + service + '\'' +
                ", path='" + path + '\'' +
                ", isCloudEvent=" + isCloudEvent +
                ", httpMethod=" + httpMethod +
                ", returnHeaders=" + returnHeaders +
                ", returnStatusCode=" + returnStatusCode +
                ", failOnStatusError=" + failOnStatusError +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, path, isCloudEvent, httpMethod, returnHeaders, returnStatusCode, failOnStatusError);
    }

    public static class Builder {

        private String service;

        private String path;

        private boolean isCloudEvent;

        private HttpMethod httpMethod = DEFAULT_HTTP_METHOD;

        private boolean returnHeaders;

        private boolean returnStatusCode;

        private boolean failOnStatusError;

        private Builder() {
        }

        public Builder withService(String service) {
            this.service = service;
            return this;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withIsCloudEvent(boolean isCloudEvent) {
            this.isCloudEvent = isCloudEvent;
            return this;
        }

        public Builder withMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder withReturnHeaders(boolean returnHeaders) {
            this.returnHeaders = returnHeaders;
            return this;
        }

        public Builder withReturnStatusCode(boolean returnStatusCode) {
            this.returnStatusCode = returnStatusCode;
            return this;
        }

        public Builder withFailOnStatusError(boolean failOnStatusError) {
            this.failOnStatusError = failOnStatusError;
            return this;
        }

        public Operation build() {
            return new Operation(this);
        }
    }
}
