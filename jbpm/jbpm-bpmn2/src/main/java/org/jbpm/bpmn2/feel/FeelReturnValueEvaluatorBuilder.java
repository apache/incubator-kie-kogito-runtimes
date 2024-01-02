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
package org.jbpm.bpmn2.feel;

import org.drools.compiler.rule.builder.PackageBuildContext;
import org.drools.drl.ast.descr.ReturnValueDescr;
import org.jbpm.process.builder.ReturnValueEvaluatorBuilder;
import org.jbpm.process.core.ContextResolver;
import org.jbpm.process.instance.impl.ReturnValueConstraintEvaluator;

public class FeelReturnValueEvaluatorBuilder implements ReturnValueEvaluatorBuilder {

    public FeelReturnValueEvaluatorBuilder() {

    }

    @Override
    public void build(final PackageBuildContext context,
            final ReturnValueConstraintEvaluator constraintNode,
            final ReturnValueDescr descr,
            final ContextResolver contextResolver) {

        String text = descr.getText();
        FeelReturnValueEvaluator expr = new FeelReturnValueEvaluator(text);
        constraintNode.setEvaluator(expr);

    }

}
