/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.bpmn2.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jbpm.bpmn2.core.*;
import org.jbpm.bpmn2.core.Error;
import org.jbpm.compiler.xml.Handler;
import org.jbpm.compiler.xml.Parser;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.compiler.xml.core.BaseAbstractHandler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ErrorHandler extends BaseAbstractHandler implements Handler {

    @SuppressWarnings("unchecked")
    public ErrorHandler() {
        if ((this.validParents == null) && (this.validPeers == null)) {
            this.validParents = new HashSet();
            this.validParents.add(Definitions.class);

            this.validPeers = new HashSet();
            this.validPeers.add(null);
            this.validPeers.add(ItemDefinition.class);
            this.validPeers.add(Message.class);
            this.validPeers.add(Interface.class);
            this.validPeers.add(Escalation.class);
            this.validPeers.add(Error.class);
            this.validPeers.add(Signal.class);
            this.validPeers.add(DataStore.class);
            this.validPeers.add(RuleFlowProcess.class);

            this.allowNesting = false;
        }
    }

    @SuppressWarnings("unchecked")
    public Object start(final String uri, final String localName,
            final Attributes attrs, final Parser parser)
            throws SAXException {
        parser.startElementBuilder(localName, attrs);

        String id = attrs.getValue("id");
        String errorCode = attrs.getValue("errorCode");
        String structureRef = attrs.getValue("structureRef");

        Definitions definitions = (Definitions) parser.getParent();

        List<Error> errors = definitions.getErrors();
        if (errors == null) {
            errors = new ArrayList<Error>();
            definitions.setErrors(errors);
            ((ProcessBuildData) parser.getData()).setMetaData("Errors", errors);
        }
        Error e = new Error(id, errorCode, structureRef);
        errors.add(e);

        return e;
    }

    public Object end(final String uri, final String localName,
            final Parser parser) throws SAXException {
        parser.endElementBuilder();
        return parser.getCurrent();
    }

    public Class<?> generateNodeFor() {
        return Error.class;
    }

}
