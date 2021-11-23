# Kogito Serverless Workflow

Kogito is an implementation of the [CNCF Serverless Workflow Specification](https://serverlessworkflow.io/).

The implementation is an ongoing effort, thus it might not be in sync with the latest features
defined in the specification.

## Current State

Currently, Kogito implements the [**version 0.6** of the specification](https://github.com/serverlessworkflow/specification/blob/0.6.x/specification.md).
The sections below describe in detail the supported features. 

### Workflow Model - States Status

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

### Workflow Model - Functions Status

| Function Type | Status             | Obs |
| ------------- | ------------------ | --- |
| rest          | :white_check_mark: | |
| rpc           | :x:                | |
| expression    | :white_check_mark: | Either `jq` or `jsonpath` |

Additionally, even though they are not defined in the specification, Kogito also supports `sysout` and `java` functions.

#### Sysout Functions

#### Java Functions