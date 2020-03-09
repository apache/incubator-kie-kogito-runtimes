package org.kie.kogito.codegen.grafana.model.panel.table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DateStyle.class, name = "date"),
        @JsonSubTypes.Type(value = NumberStyle.class, name = "number")
}
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseStyle {

    @JsonProperty("alias")
    public String alias;

    @JsonProperty("align")
    public String align;

    @JsonProperty("pattern")
    public String pattern;

    @JsonProperty("type")
    public String type;

    public BaseStyle() {
    }

    public BaseStyle(String alias, String type, String pattern, String align) {
        this.alias = alias;
        this.type = type;
        this.pattern = pattern;
        this.align = align;
    }
}