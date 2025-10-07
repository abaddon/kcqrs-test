package io.github.abaddon.kcqrs.test.counterProjection

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjectionKey
import io.github.abaddon.kcqrs.test.KcqrsProjectionTestSpecification
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.projection.CounterProjection
import io.github.abaddon.kcqrs.test.helpers.counter.projection.CounterProjectionKey
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Test case with multiple events in the given() phase
 * This verifies that projections correctly accumulate state from multiple events
 */
class MultipleEventsTest : KcqrsProjectionTestSpecification<CounterProjection>() {

    override val projectionKey: CounterProjectionKey = CounterProjectionKey()
    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 10
    private val incrementValue = 5
    private val decrementValue = 3
    private val now = Instant.now()

    private val initEvent = CounterInitialisedEvent(counterAggregateId, initialValue, 1)
    private val increaseEvent = CounterIncreasedEvent(counterAggregateId, incrementValue, 2)
    private val decreaseEvent = CounterDecreaseEvent(counterAggregateId, decrementValue, 3)
    private val finalIncreaseEvent = CounterIncreasedEvent(counterAggregateId, 2, 4)

    override fun emptyProjection(): (key: IProjectionKey) -> CounterProjection = { key ->
        CounterProjection(key as CounterProjectionKey, 0, 0, 0, now)
    }

    override fun given(): List<IDomainEvent> {
        return listOf(initEvent, increaseEvent, decreaseEvent)
    }

    override fun `when`(): IDomainEvent {
        return finalIncreaseEvent
    }

    override fun expected(): CounterProjection {
        val lastProcessedEvent: ConcurrentHashMap<String, Long> = ConcurrentHashMap()
        lastProcessedEvent[finalIncreaseEvent.aggregateType] = finalIncreaseEvent.version
        return CounterProjection(
            projectionKey,
            numCounterDecreaseEvent = 1,
            numCounterIncreasedEvent = 2,  // Two increase events total
            numCounterInitialisedEvent = 1,
            lastUpdated = now,
            lastProcessedEvent = lastProcessedEvent
        )
    }

    override fun expectedException(): Exception? {
        return null
    }
}
