# Kogito Codegen

This repository contains the shared (Maven Plug-In, Quarkus Extension, ...)
code generation logic for Kogito: processes, rules, decisions, etc.

The structure of the module is:
- `kogito-codegen-api`: shared API necessary to implement a generator
- `kogito-codegen-core`: common core classes used to wire the generation process together
- `kogito-codegen-*`: module specific generator for processes, rules, etc
- `kogito-codegen-sample`: reference module with a sample implementation of a generator (with a simple -runtime too)
- `kogito-codegen-integration-tests`: integration test modules where it is possible not only to assert on generated source code but also compile it and execute

| NOTE: the `kogito-codegen-integration-tests` module should be only used to test generate engine classes and it is not intended to be used for full end to end test (use `integration-tests` module instead) |
| ---- |

## Generator API
- Each component (process, rules, etc.) implements the `Generator`
  interface 
- `Generator`s are plugged into the `ApplicationGenerator` instance (in the `core` module)
- Upon construction, a `Generator` is given the path(s) of the directory/files
  that it must process. Scanning of the directory take place contextually
- Each `Generator` may come with its own specific configuration
- Each `Generator` can delegate to a subcomponent, to process a single
  component. E.g., the `ProcessCodegen` can
  delegate to a `ProcessGenerator` to work on a single process; `RuleCodegen`
  can delegate to a `RuleUnitGenerator`, etc.

  note: naming convention may vary in the future

- Generators **do not** write files to disk, rather return a `GeneratedFile`
  instance, with the relative file path (derived from the original path
  and further analysis on the contents of the file) and the byte array
  of the contents of the file to be dumped to disk.
  
- `KogitoBuildContext` contains all shared information about the build: it is 
  platform specific (Quarkus/Spring/Java) and it is shared by all the Generators

- `GeneratorFactory` is an interface a generator can implement and together with SPI 
  is used to automatically wire the generator (see `META-INF/services/org.kie.kogito.codegen.api.GeneratorFactory`)

## Core module
- `ApplicationGenerator` is the main entry point. The fluent API allows to
  configure its global behavior.

    ```java
    ApplicationGenerator appGen =
            new ApplicationGenerator(context)
                    .withAddons(...);
    ```
- The `ApplicationGenerator#generate()` method starts the code generation
  procedure, delegating to each `Generator` where appropriate.

### Generator wiring
The wiring of the generators can be done manually invoking the `setupGenerator` of 
`ApplicationGenerator` class
    
```java
    appGen.setupGenerator(RuleCodegen.ofPath(context, ruleSourceDirectory));
    
    appGen.setupGenerator(ProcessCodegen.ofPath(context, processSourceDirectory));
```
Or can be done via SPI using `ApplicationGeneratorDiscovery` utility class that 
automatically loads `ApplicationGenerator` and the generators

| NOTE: both Spring and Quarkus integration use SPI and `ApplicationGeneratorDiscovery` for automatic wiring |
| ---- |

## Sample generator
This generator is intended to be a prototype/reference implementation of a simple generator that
consume `.txt` files and expose the content as REST endpoint.

The example is formed by a `-runtime` module that contains the "engine" implementation (e.g. DMN runtime) and a 
`-generator` module with the generation/wiring logic

# Generated Application file

The result of the processing is the main entry point `org.kie.kogito.app.Application`.

- Components are organized into "sections". The idea, is that for a component C,
  it is possible to invoke some method such that an instance of C is returned.
  e.g.:
  
   * for process P, one may write `new Application().get(Processes.class).create("P")`
   * for rule unit R, one may write `new Application().get(RuleUnits.class).create("R")`
  
  note: specific APIs may vary.

```java
package org.kie.kogito.app;

public class Application extends org.kie.kogito.StaticApplication {

  public Application() {
    super(new ApplicationConfig());
    loadEngines(new Processes(), new RuleUnits());
  }
}
```

This Application API is intended to be accessed directly from end users and in both Spring/Quarkus scenarios it is possible
and preferable to directly inject each engine like  
```java
package org.my.package;

@ApplicationScoped
public class MyCustomBean {
  
  @Inject
  private Processes processes;

  @Inject
  private DecisionModels decisionModels;
  
  ...
}
```

# Additionally-generated files

Implementations may (and usually *do*) generate additional source code. 
In particular:
 
- Rules generate source code for the RuleUnit implementation **and** the 
  executable model description
- Processes generate source code for their Process implementation using
  their specific executable model description
- Most Generators also generate `Resources`, i.e. REST API endpoints.  

