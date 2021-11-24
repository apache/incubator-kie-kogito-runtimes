# Kogito Serverless Workflow

Kogito is an implementation of the [CNCF Serverless Workflow Specification](https://serverlessworkflow.io/).

The implementation is an ongoing effort, thus it might not be in sync with the latest features defined in the
specification.

## Current Status

Currently, Kogito implements the [**version
0.6**](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md) of the specification. The
sections below describe in detail the current status of the supported features.

### Workflow Model - States

| State         | Status             |
| ------------- | ------------------ |
| Event         | :white_check_mark: |
| Operation     | :white_check_mark: |
| Switch        | :white_check_mark: |
| Delay         | :white_check_mark: |
| Parallel      | :white_check_mark: |
| SubFlow       | :white_check_mark: |
| Inject        | :white_check_mark: |
| ForEach       | :x:                |
| Callback      | :white_check_mark: |

### Workflow Model - Functions

| Function Type | Status             | Obs |
| ------------- | ------------------ | --- |
| rest          | :white_check_mark: | You can find more details about the Kogito OpenAPI implementation [here](../kogito-codegen-modules/kogito-codegen-openapi) |
| rpc           | :x:                | |
| expression    | :white_check_mark: | Either `jq` or `jsonpath` |

Additionally, even though they are not defined in the specification, Kogito also supports `sysout` and `java` functions.

#### Sysout Functions

This function support can be used for debugging reasons:

```json
{
  "functions": [
    {
      "name": "printMessage",
      "metadata": {
        "type": "sysout"
      }
    }
  ]
}
```

Later in your State definition you can call it with:

```json
{
  "states": [
    {
      "name": "myState",
      "type": "operation",
      "actions": [
        {
          "name": "printAction",
          "functionRef": {
            "refName": "printMessage",
            "arguments": {
              "message": "."
            }
          }
        }
      ]
    }
  ]
}
```

You should see the data output in your console.

#### Java Functions

Kogito also supports calling Java functions within the maven project which the workflow is defined. You can declare your
functions like this:

```json
{
  "functions": [
    {
      "name": "myFunction",
      "metadata": {
        "interface": "com.acme.MyInterfaceOrClass",
        "operation": "myMethod",
        "type": "service"
      }
    }
  ]
}
```

Your method's interface **must** receive a Jackson's `JsonNode` object and return either `void` or another `JsonNode`.
For example:

```java
public class MyInterfaceOrClass {

    public void myMethod(JsonNode workflowData) {
        // do whatever I want with the JsonNode:
        // { "workflowdata": {} }
    }

    public JsonNode myMethod(JsonNode workflowData) {
        // do whatever I want with the JsonNode:
        // { "workflowdata": {} }
        // return the modified content:
        return workflowData;
    }
}
```

To call this function within your workflow you can extract the json value you need via a `jq` expression or pass it
without any arguments. In this case the whole payload is sent.

For example:

```json
{
  "states": [
    {
      "name": "myState",
      "type": "operation",
      "actions": [
        {
          "name": "callJavaFunctionAction",
          "functionRef": {
            "refName": "myFunction"
          }
        }
      ]
    }
  ]
}
```

Or, if you prefer you can pass only the necessary data:

```json
{
  "states": [
    {
      "name": "myState",
      "type": "operation",
      "actions": [
        {
          "name": "callJavaFunctionAction",
          "functionRef": {
            "refName": "myFunction",
            "arguments": {
              "data": ".my.path.to.data"
            }
          }
        }
      ]
    }
  ]
}
```

### Workflow Model - Events

| Definition | Status             |
| ---------- | ------------------ |
| Name       | :white_check_mark: |
| Source     | :white_check_mark: |
| Type       | :white_check_mark: |
| Kind       | :white_check_mark: |
| Correlation | :x:               |
| Metadata    | :white_check_mark: |

### Workflow Model - Retries

Kogito **does not**
support [retries](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md#Retry-Definition) just
yet.

## Workflow Data

Data manipulation (filtering and transformation) on Kogito is fully implemented and can be used either `jq`
or `jsonpath`.

## Workflow Expressions

Kogito supports either `jq` or `jsonpath` to define workflow expressions. As defined in the specification, `jq` is the
default expression language. If you wish to use `jsonpath` instead, set the attribute `expressionLang` in the workflow
definition:

```json
{
  "id": "myworkflow",
  "version": "1.0",
  "expressionLang": "jsonpath",
  "name": "Workflow example",
  "description": "An example of how to use workflows"
}
```

## Workflow Error Handling

Kogito supports error handling. Find more details about this implementation on
our [documentation](https://docs.jboss.org/kogito/release/latest/html_single/#con-serverless-workflow-error-handling_kogito-developing-decision-services)
.

## Workflow Compensation

Kogito supports workflow compensation as described in
the [specification](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md#Workflow-Compensation)
.
