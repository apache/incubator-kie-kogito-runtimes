/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.event;

/**
 * Includes all naming required for Kogito CloudEvent constants.
 * Must respect the required naming from the CloudEvent Specification.
 *
 * @see <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md#attribute-naming-convention">Attribute Naming Convention</a>
 */
public final class CloudEventExtensionConstants {

    public static final String PROCESS_INSTANCE_ID = "procinstanceid";
    public static final String PROCESS_REFERENCE_ID = "referenceid";
    public static final String PROCESS_INSTANCE_STATE = "procinstancestate";
    public static final String PROCESS_ID = "procid";
    public static final String PROCESS_PARENT_PROCESS_INSTANCE_ID = "parentprocinstanceid";
    public static final String PROCESS_ROOT_PROCESS_INSTANCE_ID = "rootprocinstanceid";
    public static final String PROCESS_ROOT_PROCESS_ID = "rootprocid";
    public static final String PROCESS_START_FROM_NODE = "startfromnode";
    public static final String ADDONS = "addons";

    private CloudEventExtensionConstants() {
        // utility class
    }

}
