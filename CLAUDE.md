# kcqrs-test Library Context

## Overview
**kcqrs-test** is a Kotlin test library that provides testing templates for the kcqrs-core library (https://github.com/abaddon/kcqrs-core). It implements base test specifications for Event Sourcing and CQRS patterns, specifically for testing Aggregates and Projections.

## Purpose
This library provides abstract test specifications that enable developers to write standardized tests for:
- **Aggregate behavior** (command handling and event generation)
- **Projection behavior** (event handling and state updates)

## Core Components

### 1. KcqrsAggregateTestSpecification<TAggregate>
**Location**: `src/main/kotlin/io/github/abaddon/kcqrs/test/KcqrsAggregateTestSpecification.kt`

Abstract base class for testing Aggregate command handlers using Given-When-Then pattern.

**Key Methods to Implement**:
- `given()`: List of events to initialize the aggregate state
- `when()`: The command to test
- `expected()`: List of expected events after command execution
- `expectedException()`: Expected exception (null if no exception expected)
- `emptyAggregate()`: Factory function to create empty aggregate
- `streamNameRoot()`: Root name for event stream
- `membersToIgnore()`: Properties to ignore during comparison (optional)
- `onCommandHandler()`: Custom command handler (optional, defaults to SimpleAggregateCommandHandler)

**Test Flow**:
1. Loads given events into the in-memory repository
2. Executes the command via the command handler
3. Compares generated events with expected events OR compares thrown exception with expected exception
4. Uses `kustomCompare` library for deep object comparison (ignoring messageId by default)

**Example Usage**: See `src/test/kotlin/io/github/abaddon/kcqrs/test/counterAggregate/InitialiseCounterTest.kt`

### 2. KcqrsProjectionTestSpecification<TProjection>
**Location**: `src/main/kotlin/io/github/abaddon/kcqrs/test/KcqrsProjectionTestSpecification.kt`

Abstract base class for testing Projection event handlers using Given-When-Then pattern.

**Key Methods to Implement**:
- `given()`: List of events to initialize the projection state
- `when()`: The single event to test
- `expected()`: Expected projection state after event application
- `expectedException()`: Expected exception (null if no exception expected)
- `emptyProjection()`: Factory function to create empty projection
- `onProjectionHandler()`: Custom projection handler (optional, defaults to SimpleProjectionHandler)

**Test Flow**:
1. Applies given events to initialize projection state
2. Applies the test event
3. Compares actual projection state with expected state using equals()
4. Handles exceptions if expected

**Example Usage**: See `src/test/kotlin/io/github/abaddon/kcqrs/test/counterProjection/InitialiseCounterTest.kt`

### 3. InMemoryEventStoreForTestRepository<TAggregate>
**Location**: `src/main/kotlin/io/github/abaddon/kcqrs/test/InMemoryEventStoreForTestRepository.kt`

In-memory implementation of EventStoreRepository for testing purposes.

**Features**:
- Stores events in a mutable map (streamName -> list of events)
- Supports projection handler subscription
- Provides test-specific methods: `addEventsToStorage()` and `loadEventsFromStorage()`
- No external dependencies (pure in-memory storage)

## Test Helpers (Example Domain)

The library includes a complete "Counter" example domain under `src/test/kotlin/io/github/abaddon/kcqrs/test/helpers/counter/`:

**Aggregate Components**:
- `CounterAggregateRoot`: Aggregate that manages a counter value
- `CounterAggregateId`: Aggregate identifier
- Commands: `InitialiseCounterCommand`, `IncreaseCounterCommand`, `DecreaseCounterCommand`
- Events: `CounterInitialisedEvent`, `CounterIncreasedEvent`, `CounterDecreaseEvent`, `DomainErrorEvent`

**Projection Components**:
- `CounterProjection`: Projection that counts event occurrences
- `CounterProjectionKey`: Projection identifier

**Example Tests**:
- Aggregate tests: `counterAggregate/InitialiseCounterTest.kt`, `IncreaseCounterTest.kt`, `DecreaseCounterTest.kt`
- Projection tests: `counterProjection/InitialiseCounterTest.kt`, `IncreaseCounterTest.kt`, `DecreaseCounterTest.kt`

## Dependencies

**Key Dependencies** (from `gradle/libs.versions.toml`):
- `kcqrs-core` - The core library being tested
- `kotlin-coroutines` - Coroutine support
- `kotlin-coroutinesTest` - Test coroutines (TestScope, UnconfinedTestDispatcher)
- `kustomCompare` - Deep object comparison for assertions
- `junit-jupiter` - JUnit 5 test framework
- `jackson-module-kotlin` - JSON serialization

## Build Configuration

- **Language**: Kotlin 2.1.20
- **JVM Target**: Java 21
- **Build Tool**: Gradle with Kotlin DSL
- **Publishing**: Maven Central via nexus-publish-plugin
- **Versioning**: Git-based versioning (tags for releases, SNAPSHOT for development)
- **Code Coverage**: JaCoCo

## Testing Philosophy

The library enforces Given-When-Then test structure:
1. **Given**: Set up initial state via events
2. **When**: Execute command or apply event
3. **Then**: Assert expected outcome (events/state) or exception

This ensures tests are:
- Reproducible (in-memory storage, no side effects)
- Isolated (each test starts fresh)
- Declarative (clear separation of setup, action, assertion)

## Important Notes for Claude

1. **Always plan before making changes** - think through the implications
2. **Always test any changes** - run the test suite after modifications
3. **Test specifications are abstract** - they must be extended with concrete implementations
4. **Coroutine testing** - Uses `UnconfinedTestDispatcher` and `TestScope` for deterministic async testing
5. **Event comparison** - `messageId` and custom members (via `membersToIgnore()`) are excluded from comparison
6. **Exception testing** - Both exception type and message are validated
7. **In-memory only** - All test infrastructure is in-memory, no external dependencies required

## File Organization

```
src/
├── main/kotlin/io/github/abaddon/kcqrs/test/
│   ├── KcqrsAggregateTestSpecification.kt       # Aggregate test template
│   ├── KcqrsProjectionTestSpecification.kt      # Projection test template
│   └── InMemoryEventStoreForTestRepository.kt   # In-memory event store
└── test/kotlin/io/github/abaddon/kcqrs/test/
    ├── helpers/counter/                         # Example domain for testing
    │   ├── entities/                            # Aggregate and ID
    │   ├── commands/                            # Command definitions
    │   ├── events/                              # Event definitions
    │   └── projection/                          # Projection and key
    ├── counterAggregate/                        # Aggregate tests
    └── counterProjection/                       # Projection tests
```