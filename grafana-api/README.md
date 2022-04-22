Grafana API
==============

This repository contains the library to create and customize grafana dashboards.

- `GrafanaConfigurationWriter` is the class that implements the logic to build dashboards for a specific DRL or DMN endpoint.
    
    The following method will simply customize the template for the endpoint
    ```java
    public static String generateDashboardForEndpoint(String templatePath, String handlerName);
    ```
    
    The following method instead will also add some panels depending on the decisions included in the DMN model.
    ```java
    public static String generateDashboardForDMNEndpoint(String templatePath, String endpoint, List<Decision> decisions);
    ```

- For full customization it is possible to use the class `JGrafana`, that contains all the properties to fully customize a dashboard.

- Kogito generates json files to populate grafana. If you do not need these files or you can not write these files on production environment, please remember to include ```kogito.quarkus.codegen.SkipDumpFiles=true``` either on your application.properties or maven pom file