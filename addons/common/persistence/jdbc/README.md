# Kogito JDBC Persistence Add-on

The Kogito JDBC Persistence Add-on adds persistence capability to Kogito projects. See the [official documentation](https://docs.jboss.org/kogito/release/latest/html_single/#con-persistence_kogito-developing-process-services) to find out more.

Currently tested for Postgres. Other database will automatically use ANSI standard SQL.

To enable JDBC persistence set the following value:
```
kogito.persistence.type=jdbc
```

Example configuration for postgres
```
# On Quarkus
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=postgres
quarkus.datasource.password=changeme
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/kogito

# On Spring Boot
spring.datasource.username=kogito-user
spring.datasource.password=kogito-pass
spring.datasource.url=jdbc:postgresql://localhost:5432/kogito
```

## Auto DLL creation
JDBC Persistence Add-on will attempt to automatically generate the necessary database objects if you enable the `autoDLL` property.
```
kogito.persistence.auto.ddl=true
```
This settings is defaulted to true.
