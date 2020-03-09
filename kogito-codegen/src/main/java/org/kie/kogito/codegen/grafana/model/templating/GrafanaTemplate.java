package org.kie.kogito.codegen.grafana.model.templating;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrafanaTemplate {

    @JsonProperty("allFormat")
    public String allFormat;

    @JsonProperty("current")
    public GrafanaTemplateCurrent current;

    @JsonProperty("datasource")
    public String datasource;

    @JsonProperty("includeAll")
    public boolean includeAll;

    @JsonProperty("name")
    public String name;

    @JsonProperty("options")
    public List<GrafanaTemplateOption> options;

    @JsonProperty("query")
    public String query;

    @JsonProperty("refresh")
    public String refresh;

    @JsonProperty("type")
    public String type;
}