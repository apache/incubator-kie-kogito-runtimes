package org.kie.kogito.codegen.grafana.model.panel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrafanaGridPos {
    @JsonProperty("x")
    public int x;

    @JsonProperty("y")
    public int y;

    @JsonProperty("w")
    public int w;

    @JsonProperty("h")
    public int h;

    public GrafanaGridPos(){}

    public GrafanaGridPos(int x, int y, int w, int h){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public String toString(){
        return String.format("{\"x\": %d, \"y\": %d, \"w\": %d, \"h\": %d}", x, y, w, h);
    }
}
