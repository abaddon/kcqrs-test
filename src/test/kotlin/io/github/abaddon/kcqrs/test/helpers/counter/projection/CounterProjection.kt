package io.github.abaddon.kcqrs.test.helpers.counter.projection

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterInitialisedEvent
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

data class CounterProjection(
    override val key: CounterProjectionKey,
    val numCounterDecreaseEvent: Int,
    val numCounterIncreasedEvent: Int,
    val numCounterInitialisedEvent: Int,
    override val lastUpdated: Instant?,
    override val lastProcessedEvent: ConcurrentHashMap<String, Long> = ConcurrentHashMap()
) : IProjection {

    override fun applyEvent(event: IDomainEvent): CounterProjection {
        return when (event) {
            is CounterDecreaseEvent -> this.copy(numCounterDecreaseEvent = numCounterDecreaseEvent + 1)
            is CounterIncreasedEvent -> this.copy(numCounterIncreasedEvent = numCounterIncreasedEvent + 1)
            is CounterInitialisedEvent -> this.copy(numCounterInitialisedEvent = numCounterInitialisedEvent + 1)
            else -> this
        }
    }

    override fun withPosition(event: IDomainEvent): IProjection {
        this.lastProcessedEvent[event.aggregateType] = event.version
        return this
    }

}
