/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process.builder;

import java.util.List;

import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.drools.compiler.lang.descr.ActionDescr;
import org.drools.compiler.lang.descr.ProcessDescr;
import org.jbpm.process.builder.dialect.ProcessDialect;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;
import org.jbpm.workflow.core.impl.NodeImpl;

public class ExtendedNodeBuilder
    implements
    ProcessNodeBuilder {

    public void build(Process process,
                      ProcessDescr processDescr,
                      ProcessBuildContext context,
                      Node node) {
        ExtendedNodeImpl extendedNode = ( ExtendedNodeImpl ) node;
        for (String type: extendedNode.getActionTypes()) {
        	List<DroolsAction> actions = extendedNode.getActions(type);
        	if (actions != null) {
	        	for (DroolsAction droolsAction: actions) {
	                buildAction(droolsAction, context, (NodeImpl) node);
	        	}
        	}
        }
    }

    protected void buildAction(DroolsAction droolsAction, ProcessBuildContext context, NodeImpl node) {
    	DroolsConsequenceAction action = (DroolsConsequenceAction) droolsAction;
        ActionDescr actionDescr = new ActionDescr();
        actionDescr.setText( action.getConsequence() );
        actionDescr.setResource(context.getProcessDescr().getResource());

        ProcessDialect dialect = ProcessDialectRegistry.getDialect( action.getDialect() );
    	dialect.getActionBuilder().build( context, action, actionDescr, node);
    }

}
