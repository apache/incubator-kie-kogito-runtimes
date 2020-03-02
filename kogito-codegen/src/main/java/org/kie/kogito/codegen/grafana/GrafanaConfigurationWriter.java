package org.kie.kogito.codegen.grafana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.developer.IJGrafana;
import com.redhat.developer.JGrafana;
import com.redhat.developer.model.panel.PanelType;

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

    public static String generateDashboardForDMNEndpoint(String handlerName, List<String> decisionNames) {
        String template = readStandardDashboard();
        template = template.replaceAll("\\$handlerName\\$", handlerName);
        template = template.replaceAll("\\$id\\$", String.valueOf(new Random().nextInt()));
        template = template.replaceAll("\\$uid\\$", UUID.randomUUID().toString());

        try {
            IJGrafana jgrafana = JGrafana.parse(template);
            decisionNames.forEach(x -> jgrafana.addPanel(PanelType.GRAPH, "Decision " + x, String.format("dmn_result{handler = \"%s\"}", x)));
            return jgrafana.serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "{}";
    }
}