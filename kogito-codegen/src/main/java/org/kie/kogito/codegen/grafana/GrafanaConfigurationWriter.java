package org.kie.kogito.codegen.grafana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.developer.IJGrafana;
import com.redhat.developer.JGrafana;
import com.redhat.developer.model.functions.GrafanaFunction;
import com.redhat.developer.model.functions.IncreaseFunction;
import com.redhat.developer.model.panel.PanelType;
import org.kie.dmn.model.v1_2.TDecision;

public class GrafanaConfigurationWriter {

    public static String readStandardDashboard() {

        InputStream is = GrafanaConfigurationWriter.class.getResourceAsStream("/grafana-dashboard-template/dashboard-template.json");
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
    }

    public static String generateDashboardForEndpoint(String handlerName) {
        String template = readStandardDashboard();
        template = template.replaceAll("\\$handlerName\\$", handlerName);
        template = template.replaceAll("\\$id\\$", String.valueOf(new Random().nextInt()));
        template = template.replaceAll("\\$uid\\$", UUID.randomUUID().toString());

        return template;
    }

    public static String generateDashboardForDMNEndpoint(String handlerName, List<TDecision> decisions) {
        String template = readStandardDashboard();
        template = template.replaceAll("\\$handlerName\\$", handlerName);
        template = template.replaceAll("\\$id\\$", String.valueOf(new Random().nextInt()));
        template = template.replaceAll("\\$uid\\$", UUID.randomUUID().toString());

        IJGrafana jgrafana;
        try {
            jgrafana = JGrafana.parse(template);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO something;
            return null;
        }

        for (TDecision decision : decisions){
            String type = decision.getVariable().getTypeRef().getLocalPart();
            if (SupportedDecisionTypes.isSupported(type)){
                jgrafana.addPanel(PanelType.GRAPH, "Decision " + decision.getName(), String.format("%s_dmn_result{handler = \"%s\"}", type, decision.getName()), SupportedDecisionTypes.getGrafanaFunction(type));
            }
        }

        try {
            return jgrafana.serialize();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}