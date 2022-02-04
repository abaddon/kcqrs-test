package io.github.abaddon.kcqrs.test.integration.counter.commands

import io.github.abaddon.kcqrs.core.domain.messages.commands.Command
import io.github.abaddon.kcqrs.test.integration.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.integration.counter.entities.CounterAggregateRoot

data class DecreaseCounterCommand(
    override val aggregateID: CounterAggregateId,
    val value: Int
    ): Command<CounterAggregateRoot>(aggregateID) {

    override fun execute(currentAggregate: CounterAggregateRoot?): CounterAggregateRoot {
        requireNotNull(currentAggregate)
        return currentAggregate.decreaseCounter(value)
    }
}