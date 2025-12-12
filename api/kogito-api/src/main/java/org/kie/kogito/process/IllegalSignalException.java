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
package org.kie.kogito.process;

import java.io.Serial;

public class IllegalSignalException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1642367053636639090L;

    private final String processInstanceId;
    private final String signal;

    public IllegalSignalException(String processInstanceId, String signal) {
        super(String.format("Process instance %s is not currently accepting signal '%s'", processInstanceId, signal));
        this.processInstanceId = processInstanceId;
        this.signal = signal;
    }

    public IllegalSignalException(String processInstanceId, String signal, Throwable cause) {
        super(String.format("Process instance %s is not currently accepting signal '%s'", processInstanceId, signal), cause);
        this.processInstanceId = processInstanceId;
        this.signal = signal;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getSignal() {
        return signal;
    }

}
