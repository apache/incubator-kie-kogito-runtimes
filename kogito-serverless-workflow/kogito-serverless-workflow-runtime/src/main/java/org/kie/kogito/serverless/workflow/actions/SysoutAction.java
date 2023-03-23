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
package org.kie.kogito.serverless.workflow.actions;

import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.internal.utils.ConversionUtils;

public class SysoutAction extends BaseExpressionAction {

    private final WorkflowLogLevel logLevel;

    public SysoutAction(String lang, String expr, String inputVar, String level) {
        super(lang, expr, inputVar);
        logLevel = ConversionUtils.isEmpty(level) ? WorkflowLogLevel.INFO : WorkflowLogLevel.valueOf(level);
    }

    @Override
    public void execute(KogitoProcessContext context) throws Exception {
        WorkflowLogger.log(logLevel, expr.isValid() ? evaluate(context, String.class) : expr.asString());
    }
}
