package org.kie.kogito.codegen.grafana.model.panel;

public enum PanelType {
    GRAPH {
        @Override
        public String toString() {
            return "graph";
        }
    },
    STAT {
        @Override
        public String toString() {
            return "stat";
        }
    },
    SINGLESTAT {
        @Override
        public String toString() {
            return "singleStat";
        }
    },
    TABLE {
        @Override
        public String toString() {
            return "table";
        }
    },
    HEATMAP {
        @Override
        public String toString() {
            return "heatmap";
        }
    },
    GAUGE {
        @Override
        public String toString() {
            return "gauge";
        }
    }
}
