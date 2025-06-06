package io.github.abaddon.kcqrs.test.counterProjection

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjectionKey
import io.github.abaddon.kcqrs.test.KcqrsProjectionTestSpecification
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.projection.CounterProjection
import io.github.abaddon.kcqrs.test.helpers.counter.projection.CounterProjectionKey
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class IncreaseCounterTest() : KcqrsProjectionTestSpecification<CounterProjection>() {

    override val projectionKey: CounterProjectionKey = CounterProjectionKey()
    private val counterAggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2
    private val now = Instant.now()
    private val event = CounterIncreasedEvent(counterAggregateId, incrementValue, 2)

    override fun emptyProjection(): (key: IProjectionKey) -> CounterProjection = { key ->
        CounterProjection(key as CounterProjectionKey, 0, 0, 0, now)
    }

    override fun given(): List<IDomainEvent> {
        return listOf(
            CounterInitialisedEvent(counterAggregateId, initialValue, 1),
        )
    }

    override fun `when`(): IDomainEvent {
        return event
    }

    override fun expected(): CounterProjection {
        val lastProcessedEvent: ConcurrentHashMap<String, Long> = ConcurrentHashMap()
        lastProcessedEvent[event.aggregateType] = event.version
        return CounterProjection(projectionKey, 0, 1, 1, now, lastProcessedEvent)
    }

    override fun expectedException(): Exception? {
        return null
    }


}