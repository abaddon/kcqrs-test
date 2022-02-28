package io.github.abaddon.kcqrs.test.helpers.counter.projection

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterInitialisedEvent

data class CounterProjection(
    override val key: CounterProjectionKey,
    val numCounterDecreaseEvent: Int,
    val numCounterIncreasedEvent: Int,
    val numCounterInitialisedEvent: Int,
): IProjection{

    override fun applyEvent(event: IDomainEvent): CounterProjection {
        return when(event){
            is CounterDecreaseEvent -> this.copy(numCounterDecreaseEvent =+1 )
            is CounterIncreasedEvent -> this.copy(numCounterIncreasedEvent =+1 )
            is CounterInitialisedEvent -> this.copy(numCounterInitialisedEvent =+1 )
            else -> this
        }
    }

}
