package io.github.abaddon.kcqrs.test.repository

import io.github.abaddon.kcqrs.test.InMemoryEventStoreForTestRepository
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterInitialisedEvent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for InMemoryEventStoreForTestRepository
 * These tests verify the core repository functionality using the public API
 */
class InMemoryEventStoreForTestRepositoryTest {

    private lateinit var repository: InMemoryEventStoreForTestRepository<CounterAggregateRoot>
    private lateinit var aggregateId: CounterAggregateId

    @BeforeEach
    fun setup() {
        aggregateId = CounterAggregateId(UUID.randomUUID())
        repository = InMemoryEventStoreForTestRepository(
            "test-stream",
            { id -> CounterAggregateRoot(id as CounterAggregateId) }
        )
    }

    @Test
    fun `should generate correct stream name for aggregate`() {
        val streamName = repository.aggregateIdStreamName(aggregateId)
        assertEquals("test-stream.${aggregateId.valueAsString()}", streamName)
    }

    @Test
    fun `should add and load events successfully`() = runTest {
        val event1 = CounterInitialisedEvent(aggregateId, 10, 1)
        val event2 = CounterIncreasedEvent(aggregateId, 5, 2)
        val events = listOf(event1, event2)

        // Add events using public API
        val addResult = repository.addEventsToStorage(aggregateId, events)
        assertTrue(addResult.isSuccess, "Add events should succeed")

        // Load events using public API
        val loadResult = repository.loadEventsFromStorage(aggregateId)
        assertTrue(loadResult.isSuccess, "Load events should succeed")

        val loadedEvents = loadResult.getOrThrow()
        assertEquals(2, loadedEvents.size, "Should load 2 events")
        assertEquals(event1, loadedEvents[0], "First event should match")
        assertEquals(event2, loadedEvents[1], "Second event should match")
    }

    @Test
    fun `should return empty list when loading from non-existent aggregate`() = runTest {
        val nonExistentId = CounterAggregateId(UUID.randomUUID())

        val loadResult = repository.loadEventsFromStorage(nonExistentId)
        assertTrue(loadResult.isSuccess, "Load should succeed even for non-existent aggregate")

        val loadedEvents = loadResult.getOrThrow()
        assertEquals(0, loadedEvents.size, "Should return empty list for non-existent aggregate")
    }

    @Test
    fun `should append events to existing aggregate`() = runTest {
        val event1 = CounterInitialisedEvent(aggregateId, 10, 1)
        val event2 = CounterIncreasedEvent(aggregateId, 5, 2)
        val event3 = CounterIncreasedEvent(aggregateId, 3, 3)

        // Add first batch
        repository.addEventsToStorage(aggregateId, listOf(event1, event2))

        // Add second batch
        val addResult = repository.addEventsToStorage(aggregateId, listOf(event3))
        assertTrue(addResult.isSuccess, "Second add should succeed")

        // Load all events
        val loadedEvents = repository.loadEventsFromStorage(aggregateId).getOrThrow()
        assertEquals(3, loadedEvents.size, "Should have 3 events total")
        assertEquals(event1, loadedEvents[0])
        assertEquals(event2, loadedEvents[1])
        assertEquals(event3, loadedEvents[2])
    }

    @Test
    fun `should create empty aggregate with provided factory`() {
        val emptyAggregate = repository.emptyAggregate(aggregateId)

        assertEquals(aggregateId, emptyAggregate.id, "Aggregate should have correct ID")
        assertEquals(0L, emptyAggregate.version, "Empty aggregate should have version 0")
        assertEquals(0, emptyAggregate.counter, "Empty counter should be 0")
    }

    @Test
    fun `should handle multiple aggregates in separate streams`() = runTest {
        val aggregateId1 = CounterAggregateId(UUID.randomUUID())
        val aggregateId2 = CounterAggregateId(UUID.randomUUID())

        val event1 = CounterInitialisedEvent(aggregateId1, 10, 1)
        val event2 = CounterInitialisedEvent(aggregateId2, 20, 1)

        repository.addEventsToStorage(aggregateId1, listOf(event1))
        repository.addEventsToStorage(aggregateId2, listOf(event2))

        val events1 = repository.loadEventsFromStorage(aggregateId1).getOrThrow()
        val events2 = repository.loadEventsFromStorage(aggregateId2).getOrThrow()

        assertEquals(1, events1.size, "Aggregate1 should have 1 event")
        assertEquals(1, events2.size, "Aggregate2 should have 1 event")
        assertEquals(event1, events1[0], "Aggregate1 event should match")
        assertEquals(event2, events2[0], "Aggregate2 event should match")
    }

    @Test
    fun `should handle adding empty event list`() = runTest {
        val addResult = repository.addEventsToStorage(aggregateId, listOf())
        assertTrue(addResult.isSuccess, "Adding empty list should succeed")

        val loadedEvents = repository.loadEventsFromStorage(aggregateId).getOrThrow()
        assertEquals(0, loadedEvents.size, "Should have no events")
    }

    @Test
    fun `should maintain event order across multiple additions`() = runTest {
        val event1 = CounterInitialisedEvent(aggregateId, 1, 1)
        val event2 = CounterIncreasedEvent(aggregateId, 1, 2)
        val event3 = CounterIncreasedEvent(aggregateId, 1, 3)
        val event4 = CounterIncreasedEvent(aggregateId, 1, 4)

        repository.addEventsToStorage(aggregateId, listOf(event1))
        repository.addEventsToStorage(aggregateId, listOf(event2, event3))
        repository.addEventsToStorage(aggregateId, listOf(event4))

        val loadedEvents = repository.loadEventsFromStorage(aggregateId).getOrThrow()
        assertEquals(4, loadedEvents.size, "Should have 4 events")
        assertEquals(listOf(event1, event2, event3, event4), loadedEvents, "Events should be in correct order")
    }
}
