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

package org.jbpm.assembler;

import org.drools.compiler.compiler.ConfigurableSeverityResult;
import org.kie.api.definition.process.Process;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;

public class DuplicateProcess extends ConfigurableSeverityResult {

    public static final String KEY = "duplicateProcess";
    private static final int[] line = new int[0];

    public DuplicateProcess(Process process, KnowledgeBuilderConfiguration config) {
        super(process.getResource(), config, "Process with same id already exists: " + process.getId());
    }

    @Override
    public int[] getLines() {
        return line;
    }

    @Override
    protected String getOptionKey() {
        return KEY;
    }

}
