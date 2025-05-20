package io.github.abaddon.kcqrs.test.counterAggregate

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.test.KcqrsAggregateTestSpecification
import io.github.abaddon.kcqrs.test.helpers.counter.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterIncreasedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterInitialisedEvent
import java.util.*

class IncreaseCounterTest() : KcqrsAggregateTestSpecification<CounterAggregateRoot>() {

    override val aggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2

    override fun given(): List<IDomainEvent> {
        return listOf(
            CounterInitialisedEvent(aggregateId,initialValue),
        )
    }

    override fun `when`(): IncreaseCounterCommand {
        return IncreaseCounterCommand(aggregateId,incrementValue)
    }

    override fun expected(): List<IDomainEvent> {
        return listOf(CounterIncreasedEvent(aggregateId,incrementValue))
    }

    override fun expectedException(): Exception? {
        return null
    }

    override fun emptyAggregate(): (IIdentity) -> CounterAggregateRoot ={
        CounterAggregateRoot(it as CounterAggregateId)
    }

    override fun streamNameRoot(): String = "IncreaseCounterTest"

    override fun membersToIgnore(): List<String> {
        return listOf("header")
    }
}