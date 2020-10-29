[1mdiff --git a/addons/monitoring-prometheus-addon/pom.xml b/addons/monitoring-prometheus-addon/pom.xml[m
[1mindex 43c24d67bd..72e403d688 100644[m
[1m--- a/addons/monitoring-prometheus-addon/pom.xml[m
[1m+++ b/addons/monitoring-prometheus-addon/pom.xml[m
[36m@@ -7,7 +7,7 @@[m
     <artifactId>monitoring-addon</artifactId>[m
     <version>1.0.0-SNAPSHOT</version>[m
   </parent>[m
[31m-  <artifactId>monitoring-prometheus-addon</artifactId>[m
[32m+[m[32m  <artifactId>addons</artifactId>[m
   <name>Kogito :: Add-Ons :: Monitoring Prometheus</name>[m
 [m
   <description>Monitoring based on Prometheus</description>[m
[1mdiff --git a/addons/pom.xml b/addons/pom.xml[m
[1mindex 83a97ee97f..b51f3fb941 100644[m
[1m--- a/addons/pom.xml[m
[1m+++ b/addons/pom.xml[m
[36m@@ -13,7 +13,6 @@[m
   <name>Kogito :: Add-Ons</name>[m
   <description>Various Add-Ons to the runtimes modules (administration, monitoring, etc)</description>[m
   <modules>[m
[31m-    <module>monitoring-prometheus-addon</module>[m
     <module>persistence</module>[m
     <module>events</module>[m
     <module>jobs</module>[m
[36m@@ -21,6 +20,8 @@[m
     <module>process-management</module>[m
     <module>tracing</module>[m
     <module>explainability</module>[m
[32m+[m[32m    <module>monitoring-addon</module>[m
[32m+[m[32m    <module>monitoring-prometheus-addon</module>[m
   </modules>[m
 [m
   <dependencyManagement>[m
