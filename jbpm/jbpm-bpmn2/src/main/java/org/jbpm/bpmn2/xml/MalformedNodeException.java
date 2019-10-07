package org.jbpm.bpmn2.xml;

import java.text.MessageFormat;

public class MalformedNodeException extends IllegalArgumentException {

    public MalformedNodeException(String id, String name, String reason) {
        super(MessageFormat.format(
                "Node id = \"{0}\", name = \"{1}\" is malformed: \"{2}\"\n", id, name, reason));
    }
}
