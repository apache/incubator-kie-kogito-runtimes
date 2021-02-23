/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.builder;

import java.util.function.BiFunction;

import org.drools.compiler.rule.builder.PackageBuildContext;
import org.jbpm.process.instance.impl.AssignmentProducer;
import org.jbpm.workflow.core.node.Assignment;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessContext;

public interface AssignmentBuilder {

    public void build(final PackageBuildContext context,
                      final Assignment assignment,
                      final String sourceExpr,
                      final String targetExpr,
                      BiFunction<ProcessContext, NodeInstance, Object> source,
                      BiFunction<ProcessContext, NodeInstance, Object> target,
                      AssignmentProducer producer);

}
