Rebalance State Machine (Finance Domain) — Java + Spring Boot

This project implements a junior‑friendly, explicit state machine for index composition rebalancing. The domain is pure Java (no Spring in domain), with a simple application layer and optional Spring Boot infrastructure for HTTP APIs. Deterministic transitions return data objects, making testing and reasoning straightforward.

Contents
- What is inside
- Quick start
- Run the tests
- Start the app (HTTP API)
- Try it with curl
- Project layout
- Concepts and design principles
- Extending the state machine
- Using the domain without Spring
- Troubleshooting

What is inside
- Explicit transition table keyed by `(state, event)`
- Deterministic transitions returning `TransitionResult` (new state, effects, audit entry)
- Guards with clear failures and `InvalidTransitionException` for undefined pairs
- Effects are domain data; application/infrastructure executes side effects
- In‑memory adapters and REST API to demo the workflow

Quick start
Prerequisites
- JDK 21+ (toolchain set to 21 in Gradle; Java 24 can be used if you adjust the toolchain)
- Gradle (wrapper included)

Clone and build
```bash
./gradlew build
```

Run the tests
```bash
./gradlew test
```

Start the app (HTTP API)
```bash
./gradlew bootRun
# App listens on http://localhost:8080
```

Try it with curl
1) Create a draft run
```bash
curl -X POST http://localhost:8080/rebalances \
     -H 'Content-Type: application/json' \
     -d '{
           "runId":"run-1",
           "indexCode":"INDEX_XYZ",
           "effectiveDate":"2025-01-31",
           "marketDataAsOf":"2025-01-30",
           "corporateActionsAsOf":"2025-01-30"
         }'
```

2) Trigger an event (e.g., INGEST_DATA)
```bash
curl -X POST http://localhost:8080/rebalances/run-1/events/INGEST_DATA
```

3) Fetch audit history
```bash
curl http://localhost:8080/rebalances/run-1/audit
```

Project layout
```
src/main/java/com/example/rebalance/
  domain/                  # Pure Java domain (no Spring)
    RebalanceState.java
    RebalanceEvent.java
    RebalanceRun.java
    Effect.java            # sealed interface + records for effects
    AuditEntry.java
    TransitionKey.java
    TransitionDefinition.java
    TransitionResult.java
    RebalanceStateMachine.java
    InvalidTransitionException.java
    GuardFailedException.java

  app/                     # Application layer (ports + workflow)
    ports/
      RebalanceRepository.java
      AuditRepository.java
      EffectPublisher.java
      TimeProvider.java
    RebalanceWorkflowService.java

  infra/                   # Spring Boot adapters and REST API
    InMemoryRebalanceRepository.java
    InMemoryAuditRepository.java
    SystemTimeProvider.java
    LoggingEffectPublisher.java
    RebalanceApplication.java
    RebalanceController.java
```

Concepts and design principles
- Explicit transition table: `Map<TransitionKey, TransitionDefinition>` defines all valid `(state, event)` pairs. Missing pairs throw `InvalidTransitionException`.
- Guards: Small predicates throwing `GuardFailedException` with a human‑readable message.
- Effects: Domain‑only records (data). The application publishes them via `EffectPublisher`.
- Audit: Every handled event produces an `AuditEntry` with run metadata, time (from `TimeProvider`), old/new states, event, message, and effect names.
- SOLID and junior‑friendly: small classes, clear responsibilities, no framework in domain.

Extending the state machine
1) Add a new `RebalanceEvent` or `RebalanceState` if required.
2) Add an `Effect` record under `domain/Effect.java` if a new side effect is needed.
3) Register a new transition in `RebalanceStateMachine` by adding a `TransitionDefinition` to the transition map.
4) If you need persistence or messaging, extend app ports and add an infra adapter (e.g., Kafka publisher implementing `EffectPublisher`).
5) Add/adjust tests in `src/test/java/com/example/rebalance/domain`.

Using the domain without Spring
You can invoke the state machine directly in plain Java tests or tools:
```java
import com.example.rebalance.domain.*;
import java.time.*;
import java.util.*;

var run = new RebalanceRun(
    "run-1", "INDEX_XYZ", LocalDate.parse("2025-01-31"),
    RebalanceState.DRAFT,
    LocalDate.parse("2025-01-30"), LocalDate.parse("2025-01-30"),
    false,
    null, null
);

var sm = new RebalanceStateMachine();
Instant occurredAt = Instant.parse("2025-01-30T12:00:00Z");
TransitionResult res = sm.transition(run, RebalanceEvent.INGEST_DATA, occurredAt);

// res.newState(), res.effects(), res.auditEntry()
```

Troubleshooting
- Build uses JDK 21 toolchain. To use Java 24, edit `build.gradle.kts`:
  ```kotlin
  java {
      toolchain {
          languageVersion.set(JavaLanguageVersion.of(24))
      }
  }
  ```
- If `bootRun` fails, ensure ports/adapters compile and no conflicting dependencies are present. Run `./gradlew clean build` and re‑try.
- If tests don’t run, confirm they are in `src/test/java` and use JUnit Jupiter annotations.

License
MIT (or your preferred license). Add a LICENSE file if needed.
