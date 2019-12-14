# ${groupId}.${artifactId} - ${version} #

# Running

- Compile and Run

    ```
     mvn clean compile quarkus:dev
    ```

- Native Image (requires JAVA_HOME to point to a valid GraalVM)

    ```
    mvn clean package -Pnative
    ```
  
  native executable (and runnable jar) generated in `target/`

# Test your application

The generated application comes with a sample test process that allows you to verify if the application is working as expected. Simply execute following command to try it out:

```sh
curl -d '{}' -H "Content-Type: application/json" -X POST http://localhost:8080/test
```

Once successfully invoked, you should see "Hello World" printed in the console of the running application.

# Developing

Add your business assets resources (process definition, rules, decisions) into `src/main/resources`.

Add your java classes (data model, utilities, services) into `src/main/java`.

Then just build the project and run.


# Swagger documentation

The OpenAPI specification of the application can be visualized and explored in the [Swagger-UI](http://localhost:8080/swagger-ui).

The application's OpenAPI YAML file can be downloaded from [this](http://localhost:8080/openapi) location.

Client applications can be easily generated from this OpenAPI YAML file.