# data-index-service-ui project

This service allow to connect with the data-index-service when security is enabled, taking care of keycloak 
interaction to be able to access to the graphiql.  


This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvn clean compile quarkus:dev
```
NOTE: With dev mode of Quarkus you can take advantage of hot reload for business assets like processes, rules and decision
tables and java code. No need to redeploy or restart your running application.During this workshop we will create a software system for a startup travel agency called Kogito Travel Agency. The first iteration of the system will consist of a set of services that are able to deal with travel requests and the booking of hotels and flights.

## Creating a native executable

You can create a native executable using: `./mvn clean package -Pnative`.

You can then execute your binary: `./target/data-index-service-ui-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image-guide .