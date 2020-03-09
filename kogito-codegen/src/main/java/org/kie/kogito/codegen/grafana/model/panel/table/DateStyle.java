package org.kie.kogito.codegen.grafana.model.panel.table;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DateStyle extends BaseStyle {

    @JsonProperty("dateFormat")
    public String dateFormat = "YYYY-MM-DD HH:mm:ss";

    public DateStyle() {
        super("Time", "date", "Time", "auto");
    }
}
