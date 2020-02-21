package org.kie.kogito.codegen.grafana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.developer.IJGrafana;
import com.redhat.developer.JGrafana;
import com.redhat.developer.model.panel.PanelType;

public class GrafanaConfigurationWriter {

    public static String readStandardDashboard(){

        InputStream is = GrafanaConfigurationWriter.class.getResourceAsStream("/grafana-dashboard-template/dashboard-template.json" );
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
    }

    public static boolean generateDashboardForEndpoint(String handlerName, int id){
        String template = readStandardDashboard();
        template = template.replaceAll("\\$handlerName\\$", handlerName);
        template = template.replaceAll("\\$id\\$", String.valueOf(id));
        template = template.replaceAll("\\$uid\\$", UUID.randomUUID().toString());

        try {
            File file = new File("/tmp/dashboard-endpoint-" + handlerName + ".json");
            file.getParentFile().mkdirs();
            PrintWriter out = new PrintWriter(file);
            out.write(template);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean generateDashboardForDMNEndpoint(String handlerName, int id, List<String> decisionNames){
        String template = readStandardDashboard();
        template = template.replaceAll("\\$handlerName\\$", handlerName);
        template = template.replaceAll("\\$id\\$", String.valueOf(id));
        template = template.replaceAll("\\$uid\\$", UUID.randomUUID().toString());

        try{
            IJGrafana jgrafana = JGrafana.parse(template);
            decisionNames.forEach(x -> jgrafana.addPanel(PanelType.GRAPH, "Decision " + x, String.format("dmn_result{handler = \"%s\"}", x)));
            File file = new File("/tmp/dashboard-endpoint-" + handlerName + ".json");
            file.getParentFile().mkdirs();
            jgrafana.writeToFile(file);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
