package org.kie.kogito.event.process;

import java.util.Map;

public class NodeDefinitionEventBody {
    private String id;
    private String nodeName;
    private String nodeType;
    private String uniqueId;
    private Map<String, ?> metadata;

    public NodeDefinitionEventBody() {
    }
    public NodeDefinitionEventBody(String id, String nodeName, String nodeType, String uniqueId, Map<String, ?> metadata) {
        this.id = id;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.uniqueId = uniqueId;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public Map<String, ?> getMetadata() {
        return metadata;
    }

    public static NodeDefinitionEventBodyBuilder builder() {
        return new NodeDefinitionEventBodyBuilder();
    }

    public static class NodeDefinitionEventBodyBuilder {
        private String id;
        private String nodeName;
        private String nodeType;
        private String uniqueId;
        private Map<String, ?> metadata;

        public NodeDefinitionEventBodyBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public NodeDefinitionEventBodyBuilder setNodeName(String nodeName) {
            this.nodeName = nodeName;
            return this;
        }

        public NodeDefinitionEventBodyBuilder setNodeType(String nodeType) {
            this.nodeType = nodeType;
            return this;
        }

        public NodeDefinitionEventBodyBuilder setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }

        public NodeDefinitionEventBodyBuilder setMetadata(Map<String, ?> metadata) {
            this.metadata = metadata;
            return this;
        }

        public NodeDefinitionEventBody build() {
            return new NodeDefinitionEventBody(id, nodeName, nodeType, uniqueId, metadata);
        }
    }
}
