package org.kie.kogito.codegen.grafana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.stream.Collectors;


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
}
