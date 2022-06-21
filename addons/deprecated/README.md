# Deprecated Add-Ons

These add-ons are kept for backward compatibility only and will be removed in future releases. Please **do not** include
them in your project.

## List of Deprecated Add-ons

#### Quarkus Add-ons

| Add-On Description      | Deprecated Artifact ID                      | New Artifact ID                                     | Since  | 
|-------------------------|---------------------------------------------|-----------------------------------------------------|--------|
| Cloud Events            | kogito-cloudevents-quarkus-addon            | kogito-addons-quarkus-messaging                     | 1.8.0  |
| Cloud Events Common     | kogito-cloudevents-quarkus-common-addon     | kogito-addons-quarkus-messaging-common              | 1.8.0  |
| Cloud Events Multi      | kogito-cloudevents-quarkus-multi-addon      | kogito-addons-quarkus-messaging                     | 1.8.0  | 
| Events Decisions        | kogito-event-driven-decisions-quarkus-addon | kogito-addons-quarkus-events-decisions              | 1.8.0  | 
| Events Smallrye         | kogito-events-reactive-messaging-addon      | kogito-addons-quarkus-events-process                | 1.8.0  |
| Explainability          | explainability-quarkus-addon                | kogito-addons-quarkus-explainability                | 1.8.0  | 
| Jobs Management         | jobs-management-quarkus-addon               | kogito-addons-quarkus-jobs-management               | 1.8.0  | 
| Knative Eventing        | knative-eventing-addon                      | kogito-addons-quarkus-knative-eventing              | 1.6.0  |
| Mail                    | mail-quarkus-addon                          | kogito-addons-quarkus-mail                          | 1.8.0  |
| Monitoring Core         | monitoring-core-quarkus-addon               | kogito-addons-quarkus-monitoring-core               | 1.8.0  | 
| Monitoring Elastic      | monitoring-elastic-quarkus-addon            | kogito-addons-quarkus-monitoring-elastic            | 1.8.0  | 
| Monitoring Prometheus   | monitoring-prometheus-quarkus-addon         | kogito-addons-quarkus-monitoring-prometheus         | 1.8.0  | 
| Persistence ISPN Health | infinispan-quarkus-health-addon             | kogito-addons-quarkus-persistence-infinispan-health | 1.8.0  |
| Persistence Infinispan  | kogito-addons-persistence-infinispan        | kogito-addons-quarkus-persistence-infinispan        | 1.12.0 |
| Persistence Kafka       | kafka-persistence-addon                     | kogito-addons-quarkus-persistence-kafka             | 1.8.0  |
| Persistence FileSystem  | kogito-addons-persistence-filesystem        | kogito-addons-quarkus-persistence-filesystem        | 1.12.0 |
| Persistence JDBC        | kogito-addons-persistence-jdbc              | kogito-addons-quarkus-persistence-jdbc              | 1.12.0 |
| Persistence MongoDB     | kogito-addons-persistence-mongodb           | kogito-addons-quarkus-persistence-mongodb           | 1.12.0 |
| Persistence Postgresql  | kogito-addons-persistence-postgresql        | kogito-addons-quarkus-persistence-postgresql        | 1.12.0 |
| Process Management      | process-management-addon                    | kogito-addons-quarkus-process-management            | 1.8.0  | 
| Process SVG             | process-svg-quarkus-addon                   | kogito-addons-quarkus-process-svg                   | 1.8.0  | 
| REST Exception Handler  | kogito-rest-exception-handler-quarkus       | kogito-addons-quarkus-rest-exception-handler        | 1.8.0  | 
| Task Management         | task-management-quarkus-addon               | kogito-addons-quarkus-task-management               | 1.8.0  | 
| Task Notification       | task-notification-quarkus-addon             | kogito-addons-quarkus-task-notification             | 1.8.0  | 
| Tracing Decision        | tracing-decision-quarkus-addon              | kogito-addons-quarkus-tracing-decision              | 1.8.0  | 

#### Spring Boot Add-ons

| Add-On Description      | Deprecated Artifact ID                         | New Artifact ID                                 | Since  | 
|-------------------------|------------------------------------------------|-------------------------------------------------|--------|
| Cloud Events            | kogito-cloudevents-spring-boot-addon           | kogito-addons-springboot-messaging              | 1.8.0  |
| Events Decisions        | kogito-event-driven-decisions-springboot-addon | kogito-addons-springboot-events-decisions       | 1.8.0  |
| Events Kafka            | kogito-events-spring-boot-addon                | kogito-addons-springboot-events-process-kafka   | 1.8.0  |
| Explainability          | explainability-springboot-addon                | kogito-addons-springboot-explainability         | 1.8.0  | 
| Jobs Management         | jobs-management-springboot-addon               | kogito-addons-springboot-jobs-management        | 1.8.0  |
| Mail                    | mail-springboot-addon                          | kogito-addons-springboot-mail                   | 1.8.0  |
| Monitoring Core         | monitoring-core-springboot-addon               | kogito-addons-springboot-monitoring-core        | 1.8.0  | 
| Monitoring Elastic      | monitoring-elastic-springboot-addon            | kogito-addons-springboot-monitoring-elastic     | 1.8.0  | 
| Monitoring Prometheus   | monitoring-prometheus-springboot-addon         | kogito-addons-springboot-monitoring-prometheus  | 1.8.0  |
| Process Management      | process-management-springboot-addon            | kogito-addons-springboot-process-management     | 1.8.0  | 
| Process SVG             | process-svg-springboot-addon                   | kogito-addons-springboot-process-svg            | 1.8.0  |
| Persistence File System | kogito-addons-persistence-filesystem           | kogito-addons-springboot-persistence-filesystem | 1.21.0 |
| Persistence Infinispan  | kogito-addons-persistence-infinispan           | kogito-addons-springboot-persistence-infinispan | 1.21.0 |
| Persistence JDBC        | kogito-addons-persistence-jdbc                 | kogito-addons-springboot-persistence-jdbc       | 1.21.0 |
| Persistence MongoDB     | kogito-addons-persistence-mongodb              | kogito-addons-springboot-persistence-mongodb    | 1.21.0 |
| Persistence Postgresql  | kogito-addons-persistence-postgresql           | kogito-addons-springboot-persistence-postgresql | 1.21.0 |
| REST Exception Handler  | kogito-rest-exception-handler-springboot       | kogito-addons-springboot-rest-exception-handler | 1.8.0  | 
| Task Management         | task-management-springboot-addon               | kogito-addons-springboot-task-management        | 1.8.0  | 
| Task Notification       | task-notification-springboot-addon             | kogito-addons-springboot-task-notification      | 1.8.0  | 
| Tracing Decision        | tracing-decision-springboot-addon              | kogito-addons-springboot-tracing-decision       | 1.8.0  | 

#### Common Add-ons

> [1] Commons Add-ons that are not supposed to be used by end user's projects.

| Add-On Description          | Deprecated Artifact ID               | New Artifact ID                           | Since | 
|-----------------------------|--------------------------------------|-------------------------------------------|-------|
| Cloud Events Utils [1]      | cloudevents-utils                    | kogito-events-api (incorporated to core)  | 1.8.0 |
| Cloud Events [1]            | kogito-cloudevents-common-addon      | kogito-addons-cloudevents                 | 1.8.0 |
| Events Decisions [1]        | kogito-event-driven-decisions-common | kogito-addons-events-decisions            | 1.8.0 |
| Explainability [1]          | explainability-addon-common          | kogito-addons-explainability              | 1.8.0 |
| Human Task Prediction API   | kogito-task-prediction-api           | kogito-addons-human-task-prediction-api   | 1.8.0 |
| Human Task Prediction Smile | kogito-task-prediction-smile-addon   | kogito-addons-human-task-prediction-smile | 1.8.0 |
| Jobs Api [1]                | jobs-api                             | kogito-addons-jobs-api                    | 1.8.0 |
| Jobs Management [1]         | jobs-management-common               | kogito-addons-jobs-management-common      | 1.8.0 |
| Mail [1]                    | mail-common-addon                    | kogito-addons-mail                        | 1.8.0 |
| Monitoring [1]              | monitoring-core-common               | kogito-addons-monitoring-core             | 1.8.0 |
| Monitoring Elastic [1]      | monitoring-elastic-common            | kogito-addons-monitoring-elastic          | 1.8.0 |
| Monitoring Prometheus [1]   | monitoring-prometheus-common         | kogito-addons-monitoring-core             | 1.8.0 |
| Persistence File System     | filesystem-persistence-addon         | kogito-addons-persistence-filesystem      | 1.8.0 | 
| Persistence Infinispan      | infinispan-persistence-addon         | kogito-addons-persistence-infinispan      | 1.8.0 |
| Persistence JDBC            | jdbc-persistence-addon               | kogito-addons-persistence-jdbc            | 1.8.0 |
| Persistence MongoDB         | mongodb-persistence-addon            | kogito-addons-persistence-mongodb         | 1.8.0 | 
| Persistence PostgreSQL      | postgresql-persistence-addon         | kogito-addons-persistence-postgresql      | 1.8.0 |
| Process Management [1]      | process-management-common            | kogito-addons-process-management          | 1.8.0 | 
| Process SVG [1]             | process-svg-common                   | kogito-addons-process-svg                 | 1.8.0 | 
| REST Exception Handler [1]  | kogito-rest-exception-handler-common | kogito-addons-rest-exception-handler      | 1.8.0 | 
| Task Management [1]         | task-management-common               | kogito-addons-task-management             | 1.8.0 | 
| Tracing Decision API [1]    | tracing-decision-api                 | kogito-addons-tracing-decision-api        | 1.8.0 | 
| Tracing Decision Common [1] | tracing-decision-common              | kogito-addons-tracing-decision-common     | 1.8.0 | 
| Tracing TypedValue API [1]  | typedvalue-api                       | kogito-addons-tracing-typedvalue-api      | 1.8.0 |
