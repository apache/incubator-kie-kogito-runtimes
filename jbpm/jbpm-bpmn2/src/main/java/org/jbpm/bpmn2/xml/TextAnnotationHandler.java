package org.jbpm.bpmn2.xml;

import java.util.HashSet;

import org.jbpm.bpmn2.core.*;
import org.jbpm.bpmn2.core.Error;
import org.jbpm.compiler.xml.Handler;
import org.jbpm.compiler.xml.Parser;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TextAnnotationHandler extends org.jbpm.compiler.xml.core.BaseAbstractHandler implements Handler {

    public TextAnnotationHandler() {
        if (this.validParents == null && this.validPeers == null) {
            this.validParents = new HashSet<>();
            this.validParents.add(ContextContainer.class);
            this.validParents.add(Definitions.class);
            this.validPeers = new HashSet<>();
            this.validPeers.add(null);
            this.validPeers.add(ItemDefinition.class);
            this.validPeers.add(Message.class);
            this.validPeers.add(Interface.class);
            this.validPeers.add(Escalation.class);
            this.validPeers.add(Error.class);
            this.validPeers.add(Signal.class);
            this.validPeers.add(DataStore.class);
            this.validPeers.add(RuleFlowProcess.class);
            this.validPeers.add(Node.class);
            this.validPeers.add(SequenceFlow.class);
            this.validPeers.add(TextAnnotation.class);
            this.validPeers.add(MetaDataHandler.MetaDataWrapper.class);
            this.allowNesting = false;
        }
    }

    @Override
    public Object start(String uri, String localName, Attributes attrs, Parser parser) throws SAXException {
        parser.startElementBuilder(localName, attrs);
        String id = attrs.getValue("id");
        TextAnnotation annotation = new TextAnnotation();
        annotation.setId(id);
        // Register the TextAnnotation by ID in ProcessBuildData
        ((ProcessBuildData) parser.getData()).setMetaData(id, annotation);

        return annotation;
    }

    @Override
    public Object end(String uri, String localName, Parser parser) throws SAXException {
        parser.endElementBuilder();
        return null;
    }

    @Override
    public Class<?> generateNodeFor() {
        return null;
    }
}
