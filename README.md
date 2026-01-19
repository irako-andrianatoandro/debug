gRPC Streaming Project — Java

This project implements a junior‑friendly gRPC streaming service demonstrating all four gRPC streaming patterns (unary, server streaming, client streaming, bidirectional streaming). The architecture follows a simple layered approach with pure Java domain logic, no framework dependencies.

Contents
- Quick start
- Run the tests
- Running the gRPC server and client
- Project layout
- Concepts and design principles

Quick start
Prerequisites
- JDK 25+ (toolchain set in Gradle)
- Gradle (wrapper included)

Build the project
```bash
./gradlew build
```

Run the tests
```bash
./gradlew test
```

Running the gRPC server and client

Start the Server
```bash
./gradlew run --args="dev.irako.topics.grpc.api.GrpcServerApp [port]"
```
Default port is 50051 if not specified.

Run the Client
In a separate terminal:
```bash
./gradlew run --args="dev.irako.topics.grpc.api.GrpcClientApp [host] [port]"
```
Default: `localhost:50051`

Project layout
```
src/main/java/dev/irako/topics/grpc/
  model/              # Domain DTOs (immutable records)
  service/            # Pure business logic (no I/O, no side-effects)
    MessageService.java
    NotificationService.java
    SensorAggregationService.java
    ChatService.java
  data/               # gRPC server and client implementations
    StreamingServiceImpl.java
    GrpcClient.java
    ProtoConverter.java
    GrpcServerFactory.java
  api/                # Entry points for server and client
    GrpcServerApp.java
    GrpcClientApp.java
```

Concepts and design principles
- **Separation of Concerns**: Proto types are isolated to the data layer via converters
- **Immutability**: All domain DTOs are records with validation
- **Pure Service Layer**: Business logic has no side-effects or I/O
- **Error Handling**: Exceptions are wrapped and logged in the data layer
- **No Framework Dependencies**: Pure Java implementation without Spring or other frameworks

The project demonstrates all four gRPC streaming patterns:
- **Unary RPC**: Single request, single response
- **Server Streaming**: Single request, multiple responses
- **Client Streaming**: Multiple requests, single response
- **Bidirectional Streaming**: Multiple requests, multiple responses

Troubleshooting
- Build uses JDK 25 toolchain. If you need a different version, edit `build.gradle.kts`
- If tests don't run, confirm they are in `src/test/java` and use JUnit Jupiter annotations
- If you see compilation errors about missing proto classes, run `./gradlew generateProto` first

License
MIT (or your preferred license). Add a LICENSE file if needed.
