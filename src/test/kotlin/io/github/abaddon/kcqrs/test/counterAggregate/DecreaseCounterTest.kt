package io.github.abaddon.kcqrs.test.counterAggregate

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.test.KcqrsAggregateTestSpecification
import io.github.abaddon.kcqrs.test.helpers.counter.commands.DecreaseCounterCommand
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterDecreaseEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterInitialisedEvent
import java.util.UUID


class DecreaseCounterTest() : KcqrsAggregateTestSpecification<CounterAggregateRoot>() {

    override val aggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2
    private val decrementValue = 3

    override fun given(): List<IDomainEvent> {
        return listOf(
            CounterInitialisedEvent(aggregateId, initialValue, 1),
            CounterIncreasedEvent(aggregateId, incrementValue, 2)
        )
    }

    override fun `when`(): ICommand<CounterAggregateRoot> {
        return DecreaseCounterCommand(aggregateId, decrementValue)
    }

    override fun expected(): List<IDomainEvent> {
        return listOf(CounterDecreaseEvent(aggregateId, decrementValue, 3))
    }

    override fun expectedException(): Exception? {
        return null
    }

    override fun emptyAggregate(): (IIdentity) -> CounterAggregateRoot = {
        CounterAggregateRoot(it as CounterAggregateId)
    }

    override fun streamNameRoot(): String = "DecreaseCounterTest"

    override fun membersToIgnore(): List<String> {
        return listOf("header")
    }


}