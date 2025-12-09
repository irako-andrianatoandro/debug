### Junie Guideline (How we work together on this project)

This guideline explains how we will capture your requirements, design, implement, and verify features so the code stays clear, maintainable, and junior‑friendly. Use it for every new topic (e.g., deserialization, validation, http-client, persistence).

---

### 1) Principles
- Clarity first: readable code, meaningful names, short methods, simple control flow.
- Separation of concerns: domain logic isolated from I/O, frameworks, and libraries.
- Functional Core, Imperative Shell: keep the service/domain layer pure (no I/O, no shared mutable state); perform side‑effects only at the API/data edges.
- Immutability by default: prefer records and immutable DTOs, final fields, no setters, persistent/defensive copies.
- Testability: unit tests for pure functions and services; integration tests for data/integration layer.
- Extensibility: add new features by adding new packages/adapters rather than editing core logic.
- Explicit contracts: define behavior at the boundaries with interfaces and DTOs.
- Fail loud in dev, fail safe in prod: clear exceptions in domain, defensive validation at edges.
- Prefer functional programming style: compose small pure functions, use map/filter/reduce and method references when they improve clarity, and avoid side‑effects inside pipelines. Keep mutation at the edges only.

---

### 2) Architecture (Simple Layered)
- presentation/api (imperative shell): entry points for external callers (CLI/REST/SDK, or a simple facade). This is where side‑effects live (I/O, logging, environment, time).
- service/use case (functional core): application logic orchestrating operations using pure functions and immutable data.
- data/util (imperative shell): persistence, HTTP, JSON, filesystem, or any external integrations.

Notes:
- Keep dependencies one-way: presentation -> service -> data.
- Prefer plain classes and interfaces; avoid over‑engineering with extra indirections.
- Keep layers thin and readable; move logic to the service layer.
- Minimize shared state and mutation; pass values explicitly, return new values instead of mutating inputs.

---

### 3) Package‑by‑Topic Structure
Each topic gets its own top‑level package under `dev.irako.topics.<topic>`. Inside the topic, use simple layers:
- `api` — optional facade/entry point
- `service` — use cases/business/application logic
- `data` — integrations (JSON, HTTP, DB, FS)
- `model` — DTOs and simple models used at boundaries
- `util` — topic‑specific helpers (keep minimal)

Tests mirror the same structure under `src/test/java`.

Example: `src/main/java/dev/irako/topics/deserialization/...`

---

### 4) Design Pattern Selection
- Strategy: choose between multiple algorithms/implementations at runtime (e.g., JSON vs XML).
- Factory/Abstract Factory: construct data-layer integrations without exposing library types.
- Facade: present a simple API to complex subsystems.
- Builder: construct complex immutable objects.
- Functional composition: prefer composing small pure functions over inheritance.
- Algebraic modeling: use `record` classes for immutable data, `sealed` hierarchies for closed polymorphism, and `switch` pattern matching (Java 17+) to handle variants.
- Template Method: only if a fixed pipeline with overridable hooks is truly needed; prefer composition first.

Rule of thumb: choose the simplest pattern that keeps code clear, immutable, and testable.

---

### 5) Coding Standards
- Java 17+; follow existing formatting of the module; prefer final fields and immutability.
- Immutability:
  - Prefer `record` for DTOs and simple models; otherwise make fields `private final` and remove setters.
  - Return new instances instead of mutating parameters; use copy/with methods or builders for changes.
  - Avoid exposing mutable collections; use `List.copyOf`, `Map.copyOf`, or unmodifiable wrappers and defensive copies.
  - Avoid shared mutable state and static caches unless justified and well‑synchronized.
- Names: nouns for classes, verbs for use cases, avoid abbreviations.
- Methods: keep under ~20–30 lines; extract helpers if needed. Favor pure functions (no observable side‑effects) in the service layer.
- Nulls: validate inputs at the API boundary; prefer `Optional` when it semantically models absence; avoid returning null.
- Errors: prefer explicit error modeling where appropriate (see Error Handling Policy). Keep exceptions meaningful.
- Logging: use SLF4J (`logger.debug/info/warn/error`); keep logging out of core service logic when possible.
- Generics: keep type safety; avoid raw types.
- Comments: explain “why”, not “what”; keep comments sparse and accurate.

---

### 6) Dependency Management (Gradle)
- Add libraries only in the layer that needs them (typically the data layer).
- Lock versions; prefer latest stable.
- Test stack: JUnit 5, Mockito, AssertJ (add only when used).
- Do not add heavy frameworks unless a requirement demands it.

Test setup snippet (add when first tests are introduced):
```kotlin
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.assertj:assertj-core:3.26.3")
}

tasks.test { useJUnitPlatform() }
```

---

### 7) Workflow per Topic
1. Requirement intake (Definition of Ready)
   - Inputs/outputs defined with examples
   - Error cases and constraints listed
   - Performance/scale or format expectations noted
2. Design
   - Define services/use cases and data integrations; choose minimal pattern(s)
   - Decide dependencies for the data layer
3. Scaffold
   - Create the package `dev.irako.topics.<topic>` with `service`, `data`, optional `api`, and tests
4. Implement
   - Service layer first with pure functions and immutable models; data layer second; api facade last
5. Tests
   - Unit test services
   - Integration test data layer with real libs where practical
6. Wire dependencies in Gradle (only needed ones)
7. Demo in `Main` or provide a small example in `api`
8. Review with the checklist (below) and merge

---

### 8) Review Checklist (Definition of Done)
- Architecture: simple layered structure (api → service → data) respected
- Tests: meaningful unit tests; data layer covered with integration tests where relevant
- Errors: exceptions clear; third‑party exceptions wrapped in the data layer
- Logging: primarily in api/data layers; levels appropriate
- API: simple, minimal surface; good names
- Docs: Javadoc on public APIs; README or topic‑level notes if non‑obvious
- Build: Gradle compiles; tests run with `useJUnitPlatform()`
- Performance/security: obvious hotspots handled; inputs validated
- Immutability & Functional: service layer has no side‑effects; models are immutable; no leaking mutable collections
- Junior‑friendly: code and docs are understandable by a junior developer (clear names, small methods, comments about "why", and at least one usage example where non‑obvious)

---

### 9) Git & Commits
- Branch per topic: `feature/<topic>-short-description`
- Conventional Commits:
  - `feat(<topic>): add JSON deserializer`
  - `fix(<topic>): handle invalid input`
  - `test(<topic>): add adapter integration test`
  - `docs(<topic>): usage example`
- Small, focused commits tied to the use case

---

### 10) Error Handling Policy
- Validate at the edges (api) and translate errors to checked outcomes as needed.
- Service layer: prefer pure functions that either return a value or a domain error value.
  - Option A (default for this repo): throw meaningful custom exceptions for truly exceptional situations.
  - Option B (when it improves clarity): return a simple Result/Either‑like type in the service layer to model expected failures without exceptions. Keep it in the service boundary; do not expose third‑party types.
- Data layer: wrap third‑party exceptions; never leak vendor types outside the data layer.
- When using exceptions, make them informative and avoid using them for control flow.

---

### 11) Performance & Memory
- Prefer streaming/iterators for large inputs (deserialization from streams)
- Avoid premature optimization; measure first when performance is a requirement
- Keep allocations reasonable; reuse `ObjectMapper`/clients via factories

---

### 12) Security & Compliance
- Never log sensitive data
- Validate all external inputs
- Keep dependencies updated; avoid abandoned libraries

---

### 13) Topic Scaffold Template (example: deserialization)
- `model`: immutable DTOs using `record` or classes with final fields and no setters
- `service/DeserializeFromString<T>`: pure function(s) operating on immutable inputs and returning new values
- `data/JsonReader<T>`: performs I/O and mapping; isolate side‑effects here (optional dependency)
- `api/DeserializationApi`: imperative facade that validates inputs, calls pure services, and handles logging/errors (optional)
- Tests: `DeserializeFromStringTest` (unit, pure), `JsonReaderIT` (integration)

Add Jackson only if requested:
```kotlin
dependencies { implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1") }
```

---

### 14) How to Request a New Topic
Provide:
- Topic name
- Goal in one sentence
- Inputs/outputs with 1–2 examples
- Edge cases/errors to handle
- Library preference (if any)
- Performance/security notes (if any)

Example request:
```
Topic: deserialization
Goal: Convert JSON string to a User object
Input/Output: "{\"name\":\"Alice\"}" -> User(name="Alice")
Errors: invalid JSON should throw a clear exception
Library: Jackson preferred
```

---

### 15) Next Step
Tell me your first topic, and I’ll scaffold the package with ports/use cases, add minimal tests, and provide a short usage example in `Main` or an `api` facade.


---

### 16) Write for Junior Developers (Clarity Playbook)
Use these rules while coding and reviewing to ensure newcomers can understand and safely extend the code.

- Prefer clarity over cleverness:
  - Avoid cryptic one‑liners and overly smart streams; break them into well‑named steps when it reads better.
  - Choose explicit over implicit: prefer clear variable names and method calls over magic or hidden behavior.
- Name things like you explain to a teammate:
  - Methods are verbs that state intent (e.g., parseUser, validateInput, toDto).
  - Variables describe the data (e.g., rawJson, userName, lines).
- Keep functions small and single‑purpose:
  - Target ≤ 20–30 lines; extract helpers for distinct sub‑steps.
  - One level of abstraction per function; do not mix parsing, validation, and I/O together.
- Control flow that’s easy to follow:
  - Prefer early guard clauses to reduce nesting.
  - Keep boolean logic straightforward; introduce well‑named predicates if it improves readability.
- Comments that explain “why”, not “what”:
  - Add a brief rationale for non‑obvious decisions, trade‑offs, or workarounds.
  - Avoid outdated or redundant comments; keep them accurate.
- Examples where non‑obvious:
  - Public APIs and tricky methods should include a short usage example in Javadoc or in tests.
  - Add sample inputs/outputs for parsing/formatting functions.
- Make data and side‑effects obvious:
  - Default to immutable data; when mutation is required, isolate it and name it clearly.
  - Keep I/O at the edges; pure functions in the core make behavior easier to reason about.
- Be explicit with types for readability:
  - Use `var` only when the type is clear from the right‑hand side; otherwise spell out the type.
  - Avoid wildcard imports; keep imports readable.
- Errors that teach:
  - Throw exceptions with actionable messages (what went wrong, which value, and how to fix).
  - Validate inputs at boundaries and fail fast with clear messages in development.
- Tests as living documentation:
  - Write small, focused tests that demonstrate expected behavior and edge cases.
  - Name tests to read like a spec: methodName_condition_expectedResult.
- Pull requests and commits:
  - Small, incremental commits with clear messages (what/why).
  - PR description includes context, screenshots or sample I/O when useful, and a brief test plan.
