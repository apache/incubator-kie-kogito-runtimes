package org.kie.kogito.serverless.workflow.dmn;

import java.util.Map;

import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.serverless.workflow.WorkflowWorkItemHandler;

public class DMNWorkItemHandler extends WorkflowWorkItemHandler {

    public static final String FILE_PROP = "file";
    public static final String NAME = "dmn";

    @Override
    protected Object internalExecute(KogitoWorkItem workItem, Map<String, Object> parameters) {
        Map<String, Object> metadata = workItem.getNodeInstance().getNode().getMetaData();
        String file = (String) metadata.get(FILE_PROP);
        // TODO execute dmn file
        return null;
    }

}
