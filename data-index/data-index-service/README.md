# Instructions for developers

Data Index service is a Quarkus based application that aims to consume [CloudEvents](https://cloudevents.io) based messages from Kogito runtimes, process and index the information for later consumption via GraphQL queries.
These events contains information about units of work executed for a process. Visit [process-runtime-events](https://github.com/kiegroup/kogito-runtimes/wiki/Configuration#process-runtime-events) for more details about the payload of the messages.
This data is then parsed and pushed into different Infinispan caches.
These caches are structured as follows:
 - Domain cache: This cache is a generic cache, one per process id, where the process instance variables are pushed as the root content. This cache also includes
some process instance metadata, which allows to correlate data between domain and process instances. Data is transferred as JSON format to Infinispan server as a concrete Java type is not available. 
 - Process instance cache: Each process instance is pushed here containing all information, not only metadata, that includes extra information such as nodes executed.

Storage is provided by Infinispan and integrated using `quarkus-infinispan-client`. Communication between the Index Service and Infinispan is handled via [Protocol Buffers](https://developers.google.com/protocol-buffers/) which requires creating
`.proto` files and marshallers to read and write bytes. For more information, visit the [Quarkus Infinispan Client Guide](https://quarkus.io/guides/infinispan-client-guide).

In order to enable indexing of custom process models, the Data Index Service consumes proto files generated for a given process, see https://github.com/kiegroup/kogito-runtimes/wiki/Persistence#process-instance-variables.
That can be added by simply making the proto files available in a folder for the service to read once starting.
Once the proto model is parsed, a respective [GraphQL](https://graphql.org) type is generated to allow clients to query the information. Again, the process data is available
via the two caches, where users can query based on the technical aspects (Process Instances) or domain specific (proto defined type).

The Data Index Service is also a [Vert.X](https://vertx.io) based application for more details, visit https://quarkus.io/guides/using-vertx. An important aspect
provided by Vert.X is the native integration with [Java GraphQL](https://www.graphql-java.com/) provided by the [vertx-web-graphql](https://vertx.io/docs/vertx-web-graphql/java/).

Messaging integration is also provided by Quarkus using `quarkus-smallrye-reactive-messaging-kafka`. For more details and configution details, visit
https://quarkus.io/guides/kafka-guide and https://smallrye.io/smallrye-reactive-messaging/.

## Prerequisites

You will need:
 - Infinispan server:
   Download and run https://downloads.jboss.org/infinispan/10.0.0.Beta4/infinispan-server-10.0.0.Beta4.zip.
 Service will be available on port 11222. This should match the setting `quarkus.infinispan-client.server-list=localhost:11222` on `application.properties`.  

 - Kafka messaging server:
   The best way to get started is to use the a Docker Image that already contains all the necessary bits for running Kafka locally.
   For more details visit: https://hub.docker.com/r/spotify/kafka/ 
   ```
   docker run -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=localhost --env ADVERTISED_PORT=9092 spotify/kafka
   ``` 
   
## Running

As a Quarkus based application, running the service for development takes full benefit of the dev mode support with live code reloading.
For that simply run:
```
mvn clean compile quarkus:dev
```

Once the service is up and running, it will start consuming messages from the  Kafka topic named: `kogito-processinstances-events`.
For more details on how to enable a Kogito runtime to produce events, please visit: https://github.com/kiegroup/kogito-runtimes/wiki/Configuration#publishing-events

When running on dev mode, the [GraphiQL](https://github.com/graphql/graphiql) UI is available, on http://localhost:8180/, which allows
exploring and querying the available data model.

In there you can explore the current types available using the Docs section on the top right and execute queries on the model.
Some examples:

### Querying the technical cache
```json
{
  ProcessInstances {
    id
    processId
    state
    parentProcessInstanceId
    rootProcessId
    rootProcessInstanceId
    variables
    nodes {
      id
      name
      type
    }
  }
}
```  

### Querying the domain cache
Assuming a Travels model is deployed
```json
{
  Travels {
    visaApplication {
      duration
    }
    flight {
      flightNumber
      gate
    }
    hotel {
      name
      address {
        city
        country
      }
    }
    traveller {
      firstName
      lastName
      nationality
      email
    }
  }
}
``` 

### Loading proto files from file system
To bootstrap the service using a set of proto files from a folder, simply pass the following property `kogito.protobuf.folder` and any .proto file contained in the folder will automatically be loaded when the application starts.
Yo can also setup the service to reload/load any changes to files during the service execution by setting `kogito.protobuf.watch=true`.

```
mvn clean compile quarkus:dev -Dkogito.protobuf.folder=/home/git/kogito-runtimes/data-index/data-index-service/src/test/resources -Dkogito.protobuf.watch=true
```

### Building

To generate the final build artifact, sim run:

```
mvn clean install
```

The Maven build will generate a uber jar at `target/data-index-service-${version}-runner.jar`, that can be executed via:
```
java -jar target/data-index-service-8.0.0-SNAPSHOT-runner.jar
```
 
### Infinispan Indexing

Infinispan embeds a Lucene engine in order to provide indexing of models in the cache. To be able to hint the engine about which attributes should be indexed,
it needs to annotate the proto file attributes using Hibernate Search annotations  `@Indexed` and `@Field`.
For more details, visit https://infinispan.org/docs/dev/user_guide/user_guide.html#indexing_of_protobuf_encoded_entries.  

Sample indexed model:
```
/* @Indexed */
message ProcessInstanceMeta {
    /* @Field(store = Store.YES, analyze = Analyze.YES, analyzer = @Analyzer(definition = "keyword")) */
    optional string id = 1;
}
```