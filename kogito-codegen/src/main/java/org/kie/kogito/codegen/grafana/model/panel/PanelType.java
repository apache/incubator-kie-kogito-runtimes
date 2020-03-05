package org.kie.kogito.codegen.grafana.model.panel;

public enum PanelType {
    GRAPH{
        public String toString() {
            return "graph";
        }
    },
    STAT{
        public String toString() {
            return "stat";
        }
    },
    SINGLESTAT{
        public String toString() {
            return "singleStat";
        }
    },
    TABLE{
        public String toString() {
            return "table";
        }
    },
    HEATMAP{
        public String toString() {
            return "heatmap";
        }
    },
    GAUGE{
            public String toString() {
                return "gauge";
            }
    }
}
