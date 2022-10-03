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
package org.kie.kogito.serverless.workflow.parser;

public class VariableInfo {
    private final String inputVar;
    private final String outputVar;

    public VariableInfo(String inputVar, String outputVar) {
        this.inputVar = inputVar;
        this.outputVar = outputVar;
    }

    @Override
    public String toString() {
        return "VariableInfo [inputVar=" + inputVar + ", outputVar=" + outputVar + ']';
    }

    public String getInputVar() {
        return inputVar;
    }

    public String getOutputVar() {
        return outputVar;
    }
}
