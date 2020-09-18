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
package org.kie.internal.remote;

public interface PermissionConstants {

    public static final String REST_ROLE            = "rest-all";
    public static final String REST_PROJECT_ROLE    = "rest-project";
    public static final String REST_DEPLOYMENT_ROLE = "rest-deployment";
    public static final String REST_PROCESS_ROLE    = "rest-process";
    public static final String REST_PROCESS_RO_ROLE = "rest-process-read-only";
    public static final String REST_TASK_ROLE       = "rest-task";
    public static final String REST_TASK_RO_ROLE    = "rest-task-read-only";
    public static final String REST_QUERY_ROLE      = "rest-query";
    public static final String REST_CLIENT_ROLE          = "rest-client";

}
