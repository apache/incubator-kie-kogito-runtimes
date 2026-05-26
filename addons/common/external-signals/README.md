<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Kogito External Signals Addon

This addon provides support for external signal scope in Kogito runtimes, enabling BPMN processes to send signals to external systems via messaging infrastructure (Kafka, HTTP, etc.).

## Overview

The External Signals addon allows BPMN signal throw events to dispatch signals outside the process engine scope. When a signal throw event is marked with an "external" scope, instead of being processed internally by the process engine, the signal is sent to an external system through Kogito's event infrastructure.

### Key Features

- **Transport-Agnostic**: Uses Kogito's `EventEmitter` infrastructure, supporting any transport (Kafka, HTTP, etc.)
- **CloudEvent Compatible**: Signal events follow CloudEvent specifications for interoperability
- **Configurable Routing**: Map signal names to specific topics/triggers via configuration
- **Fire-and-Forget **: Signals are dispatched asynchronously without blocking the process
- **Extensible**: Designed for future enhancements (correlation, request-response patterns)

## Architecture

### Components

1. **ExternalSignalEvent** - CloudEvent-compatible signal event model
2. **ExternalSignalDispatcher** - Interface for signal dispatching
3. **ExternalSignalWorkItemHandler** - Handles "External Send Task" work items
4. **ExternalSignalConfig** - Configuration for signal-to-topic mapping

### How It Works

```
BPMN Process → Signal Throw Event (external scope) 
    → ExternalSignalWorkItemHandler 
    → ExternalSignalDispatcher 
    → EventEmitter 
    → External System (Kafka/HTTP/etc.)
```

## Installation

### Maven

Add the runtime dependency to your project:

```xml
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kogito-addons-external-signals-runtime</artifactId>
</dependency>
```

The addon is automatically discovered via ServiceLoader and registers the work item handler.

## Configuration

Configure signal-to-topic mappings using application properties:

### Properties

```properties
# Map specific signals to topics/triggers
kogito.external-signals.mapping.OrderCreated=order-events
kogito.external-signals.mapping.PaymentProcessed=payment-topic
kogito.external-signals.mapping.InventoryUpdated=inventory-updates

# Set custom default prefix for unmapped signals (default: "kogito-external-signal")
kogito.external-signals.default-prefix=my-app-signal
```

### Topic Resolution

The addon resolves topics using this priority:

1. **Explicit Mapping**: If `kogito.external-signals.mapping.{signalName}` is configured, use that value
2. **Default Convention**: Otherwise, use `{prefix}-{signalName}` where prefix defaults to `kogito-external-signal`

**Examples:**

| Signal Name | Configuration | Resolved Topic |
|------------|---------------|----------------|
| OrderCreated | `kogito.external-signals.mapping.OrderCreated=order-events` | `order-events` |
| PaymentProcessed | (no mapping) | `kogito-external-signal-PaymentProcessed` |
| InventoryUpdated | (custom prefix: `my-app`) | `my-app-InventoryUpdated` |

## Usage

### BPMN Process Definition

Create a BPMN process with a signal throw event marked with external scope:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
                   targetNamespace="http://www.kogito.org/bpmn">
  
  <bpmn2:signal id="OrderApprovalSignal" name="OrderApproval"/>
  
  <bpmn2:process id="order-process" name="Order Process">
    
    <bpmn2:startEvent id="start" name="Start"/>
    
    <!-- External Signal Throw Event -->
    <bpmn2:intermediateThrowEvent id="sendApproval" name="Send Approval Request">
      <bpmn2:signalEventDefinition signalRef="OrderApprovalSignal">
        <bpmn2:extensionElements>
          <customScope>external</customScope>
        </bpmn2:extensionElements>
      </bpmn2:signalEventDefinition>
    </bpmn2:intermediateThrowEvent>
    
    <bpmn2:endEvent id="end" name="End"/>
    
    <bpmn2:sequenceFlow sourceRef="start" targetRef="sendApproval"/>
    <bpmn2:sequenceFlow sourceRef="sendApproval" targetRef="end"/>
    
  </bpmn2:process>
</bpmn2:definitions>
```

### Work Item Parameters

The external signal work item supports these parameters:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| Signal | String | Yes | Name of the signal to send |
| Data | Object | No | Data payload to send with the signal |
| SignalProcessInstanceId | String | No | Target process instance ID (for future correlation) |
| SignalWorkItemId | String | No | Target work item ID (for future correlation) |
| SignalDeploymentId | String | No | Target deployment ID (for future correlation) |

### Event Structure

External signals are dispatched as CloudEvent-compatible events:

```json
{
  "specversion": "1.0",
  "type": "org.kie.kogito.signal.external.OrderApproval",
  "source": "kogito://process/order-123",
  "id": "signal-correlation-id",
  "time": "2024-01-15T10:30:00Z",
  "data": {
    "signalName": "OrderApproval",
    "signalData": {
      "orderId": "12345",
      "amount": 1000.00
    },
    "sourceProcessInstanceId": "order-123",
    "correlationId": "signal-correlation-id",
    "timestamp": "2024-01-15T10:30:00Z",
    "metadata": {
      "workItemId": "wi-456",
      "nodeInstanceId": "ni-789"
    }
  }
}
```

## Integration with Messaging

### Kafka

When using with Kafka, ensure you have the Kafka addon:

```xml
<dependency>
    <groupId>org.kie.kogito</groupId>
    <artifactId>kogito-addons-quarkus-messaging</artifactId>
</dependency>
```

Configure Kafka topics in `application.properties`:

```properties
# Map signals to Kafka topics
kogito.external-signals.mapping.OrderCreated=orders
kogito.external-signals.mapping.PaymentProcessed=payments

# Kafka configuration
mp.messaging.outgoing.orders.connector=smallrye-kafka
mp.messaging.outgoing.orders.topic=order-events
mp.messaging.outgoing.orders.value.serializer=org.apache.kafka.common.serialization.StringSerializer

mp.messaging.outgoing.payments.connector=smallrye-kafka
mp.messaging.outgoing.payments.topic=payment-events
mp.messaging.outgoing.payments.value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

### HTTP

For HTTP-based event emission, configure the HTTP addon accordingly.

## Current Limitations

The current implementation has these characteristics:

- **Fire-and-Forget**: Signals are dispatched asynchronously without waiting for responses
- **No Correlation**: Response correlation is not yet implemented
- **No Retry Logic**: Failed dispatches abort the work item (can be enhanced in future)

## Future Enhancements (Roadmap)

## Troubleshooting

### Signal Not Dispatched

**Problem**: External signal is not being sent to the external system.

**Solutions**:
1. Verify the addon is on the classpath
2. Check that the signal throw event has `<customScope>external</customScope>`
3. Review logs for dispatcher errors
4. Ensure EventEmitter is properly configured for your transport

### Configuration Not Applied

**Problem**: Signal mappings are not being used.

**Solutions**:
1. Verify property names: `kogito.external-signals.mapping.{SignalName}`
2. Check for typos in signal names (case-sensitive)
3. Review application startup logs for configuration loading messages

### Work Item Aborted

**Problem**: Work item is aborted instead of completing.

**Solutions**:
1. Check that the `Signal` parameter is provided and not empty
2. Review error logs for dispatch exceptions
3. Verify EventEmitter is available for the resolved trigger

## Examples

See the `external-signals-integration-tests` module for complete examples.

## API Documentation

### ExternalSignalEvent

```java
ExternalSignalEvent event = ExternalSignalEvent.builder()
    .signalName("OrderApproval")
    .signalData(orderData)
    .sourceProcessInstanceId("process-123")
    .correlationId("signal-456")
    .timestamp(Instant.now())
    .addMetadata("customKey", "customValue")
    .build();
```

### ExternalSignalDispatcher

```java
ExternalSignalDispatcher dispatcher = new DefaultExternalSignalDispatcher(config);
dispatcher.dispatch(event);
```

### ExternalSignalConfig

```java
Map<String, String> props = new HashMap<>();
props.put("kogito.external-signals.mapping.OrderCreated", "order-events");
props.put("kogito.external-signals.default-prefix", "my-app");

ExternalSignalConfig config = new ExternalSignalConfigImpl(props);
String trigger = config.resolveTrigger("OrderCreated"); // Returns: "order-events"
```

## Contributing

Contributions are welcome! Please follow the Kogito contribution guidelines.

## License

Licensed under the Apache License, Version 2.0. See LICENSE file for details.