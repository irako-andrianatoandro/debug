# gRPC Streaming Topic

This topic demonstrates all gRPC streaming patterns with a complete client-server implementation.

## Overview

The gRPC topic includes:
- **Unary RPC**: Single request, single response
- **Server Streaming**: Single request, multiple responses
- **Client Streaming**: Multiple requests, single response
- **Bidirectional Streaming**: Multiple requests, multiple responses

## Architecture

Following the project's layered architecture:

- **`model/`**: Domain DTOs (immutable records)
- **`service/`**: Pure business logic (no I/O, no side-effects)
- **`data/`**: gRPC server and client implementations, proto converters
- **`api/`**: Entry points for server and client applications

## Proto File

The proto definition is in `src/main/proto/streaming_service.proto`. It defines:
- `StreamingService` with all four RPC types
- Message types for each operation
- Enums for priorities and message types

## Building

The proto files are automatically generated during the build process:

```bash
./gradlew build
```

Or to generate proto files only:

```bash
./gradlew generateProto
```

**Note**: If you see compilation errors about missing proto classes, run `./gradlew generateProto` first, or let your IDE build the project.

## Running

### Start the Server

```bash
./gradlew run --args="dev.irako.topics.grpc.api.GrpcServerApp [port]"
```

Default port is 50051 if not specified.

### Run the Client

In a separate terminal:

```bash
./gradlew run --args="dev.irako.topics.grpc.api.GrpcClientApp [host] [port]"
```

Default: `localhost:50051`

## Examples

### Unary RPC
Sends a single message and receives a single response.

### Server Streaming
Subscribes to notifications and receives a stream of notification messages.

### Client Streaming
Uploads multiple sensor readings and receives an aggregated response.

### Bidirectional Streaming
Chat-like interface where both client and server can send messages continuously.

## Testing

Run all tests:

```bash
./gradlew test
```

The test suite includes:
- Unit tests for service layer (pure functions)
- Integration tests for gRPC server and client

## Code Structure

### Service Layer (Pure Functions)
- `MessageService`: Processes messages
- `NotificationService`: Generates notifications
- `SensorAggregationService`: Aggregates sensor data
- `ChatService`: Processes chat messages

### Data Layer (I/O)
- `StreamingServiceImpl`: gRPC service implementation
- `GrpcClient`: gRPC client wrapper
- `ProtoConverter`: Converts between proto and domain DTOs
- `GrpcServerFactory`: Factory for creating gRPC servers

### API Layer (Entry Points)
- `GrpcServerApp`: Server application entry point
- `GrpcClientApp`: Client application with examples

## Key Design Decisions

1. **Separation of Concerns**: Proto types are isolated to the data layer via converters
2. **Immutability**: All domain DTOs are records with validation
3. **Pure Service Layer**: Business logic has no side-effects or I/O
4. **Error Handling**: Exceptions are wrapped and logged in the data layer
5. **Logging**: Uses SLF4J, primarily in data/api layers

## Dependencies

- gRPC Java libraries (netty, protobuf, stub)
- Protobuf compiler plugin
- JUnit 5 for testing

All dependencies are managed in `build.gradle.kts`.
