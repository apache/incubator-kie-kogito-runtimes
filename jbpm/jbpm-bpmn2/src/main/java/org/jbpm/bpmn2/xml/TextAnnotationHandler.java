package org.jbpm.bpmn2.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

import static org.jbpm.ruleflow.core.Metadata.TEXT_ANNOTATIONS;

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
            this.validPeers.add(SequenceFlow.class);
            this.allowNesting = false;
        }
    }

    @Override
    public Object start(String uri, String localName, Attributes attrs, Parser parser) throws SAXException {
        parser.startElementBuilder(localName, attrs);
        String id = attrs.getValue("id");
        TextAnnotation annotation = new TextAnnotation();
        annotation.setId(id);
        Map<String, TextAnnotation> annotations =
                (Map<String, TextAnnotation>) ((ProcessBuildData) parser.getData()).getMetaData(TEXT_ANNOTATIONS);

        if (annotations == null) {
            annotations = new HashMap<>();
            ((ProcessBuildData) parser.getData()).setMetaData(TEXT_ANNOTATIONS, annotations);
        }
        annotations.put(id, annotation);

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
