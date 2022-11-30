/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.jobs.service.api.recipient.http;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.kie.kogito.jobs.service.api.Recipient;

@Schema(description = "Recipient definition to execute a job with a http request", allOf = { Recipient.class })
public class HttpRecipient extends Recipient<byte[]> {

    private String url;
    private String method;
    private Map<String, String> headers;
    private Map<String, String> queryParams;

    public HttpRecipient() {
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }

    public HttpRecipient(byte[] payload,
            String url,
            String method) {
        this(payload, url, method, new HashMap<>(), new HashMap<>());
    }

    public HttpRecipient(byte[] payload,
            String url,
            String method,
            Map<String, String> headers,
            Map<String, String> queryParams) {
        super(payload);
        this.url = url;
        this.method = method;
        this.headers = headers != null ? headers : new HashMap<>();
        this.queryParams = queryParams != null ? queryParams : new HashMap<>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers != null ? headers : new HashMap<>();
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams != null ? queryParams : new HashMap<>();
    }

    @Override
    public String toString() {
        return "HttpRecipient{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                "} " + super.toString();
    }
}
