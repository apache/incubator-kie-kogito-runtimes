/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.grafana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.kie.dmn.model.v1_2.TDecision;
import org.kie.kogito.grafana.dmn.SupportedDecisionTypes;
import org.kie.kogito.grafana.model.panel.PanelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrafanaConfigurationWriter {

    private static final Logger logger = LoggerFactory.getLogger(GrafanaConfigurationWriter.class);

    private static final String grafanaTemplateResourcePath = "/grafana-dashboard-template/dashboard-template.json";

    private GrafanaConfigurationWriter() {
        // Intentionally left blank.
    }

    public static String readStandardDashboard() {

        InputStream is = GrafanaConfigurationWriter.class.getResourceAsStream(grafanaTemplateResourcePath);
        return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
    }

    public static String generateDashboardForEndpoint(String handlerName) {
        String template = readStandardDashboard();
        return customizeTemplate(template, handlerName);
    }

    public static String generateDashboardForDMNEndpoint(String endpoint, List<TDecision> decisions) {
        String template = readStandardDashboard();
        template = customizeTemplate(template, endpoint);

        JGrafana jgrafana;
        try {
            jgrafana = JGrafana.parse(template);
        } catch (IOException e) {
            logger.warn("Could not read the grafana dashboard template.", e);
            return null;
        }

        for (TDecision decision : decisions) {
            String type = decision.getVariable().getTypeRef().getLocalPart();
            if (SupportedDecisionTypes.isSupported(type)) {
                jgrafana.addPanel(PanelType.GRAPH,
                                  "Decision " + decision.getName(),
                                  String.format("%s_dmn_result{endpoint = \"%s\", decision = \"%s\"}",
                                                type,
                                                endpoint,
                                                decision.getName()),
                                  SupportedDecisionTypes.getGrafanaFunction(type));
            }
        }

        try {
            return jgrafana.serialize();
        } catch (IOException e) {
            logger.warn("Could not serialize the grafana dashboard template.", e);
            return null;
        }
    }

    private static String customizeTemplate(String template, String handlerName) {
        template = template.replaceAll("\\$handlerName\\$", handlerName);
        template = template.replaceAll("\\$id\\$", String.valueOf(new Random().nextInt()));
        template = template.replaceAll("\\$uid\\$", UUID.randomUUID().toString());
        return template;
    }
}