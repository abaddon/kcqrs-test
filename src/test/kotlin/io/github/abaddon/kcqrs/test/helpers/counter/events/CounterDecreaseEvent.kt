package io.github.abaddon.kcqrs.test.helpers.counter.events

import io.github.abaddon.kcqrs.core.domain.messages.events.EventHeader
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateId
import java.util.UUID

data class CounterDecreaseEvent(
    override val messageId: UUID,
    override val aggregateId: CounterAggregateId,
    override val version: Long,
    override val aggregateType: String,
    override val header: EventHeader,
    val value: Int
) : IDomainEvent {

    constructor(aggregateId: CounterAggregateId, value: Int, version: Long) : this(
        UUID.randomUUID(),
        aggregateId,
        version,
        "CounterAggregateRoot",
        EventHeader.create("CounterAggregateRoot"),
        value
    )

}