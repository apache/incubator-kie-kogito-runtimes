package org.jbpm.flow.migration;

import java.util.Map;

import org.kie.api.io.Resource;

public class DummyProcess implements org.kie.api.definition.process.Process {

    private String id;
    private String version;

    public DummyProcess(String id, String version) {
        this.id = id;
        this.version = version;
    }

    @Override
    public KnowledgeType getKnowledgeType() {
        return null;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public Map<String, Object> getMetaData() {
        return null;
    }

    @Override
    public Resource getResource() {
        return null;
    }

    @Override
    public void setResource(Resource res) {
    }

}