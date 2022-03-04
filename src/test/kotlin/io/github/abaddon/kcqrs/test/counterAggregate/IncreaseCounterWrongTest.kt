package io.github.abaddon.kcqrs.test.counterAggregate

import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.test.KcqrsAggregateTestSpecification
import io.github.abaddon.kcqrs.test.helpers.counter.commands.IncreaseCounterCommand
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateId
import io.github.abaddon.kcqrs.test.helpers.counter.entities.CounterAggregateRoot
import io.github.abaddon.kcqrs.test.helpers.counter.events.CounterInitialisedEvent
import io.github.abaddon.kcqrs.test.helpers.counter.events.DomainErrorEvent
import java.util.*

class IncreaseCounterWrongTest() : KcqrsAggregateTestSpecification<CounterAggregateRoot>(){

    override val aggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 5
    private val incrementValue = 2147483647

    override fun given(): List<IDomainEvent> {
        return listOf(
            CounterInitialisedEvent(aggregateId, initialValue),
        )
    }

    override fun `when`(): IncreaseCounterCommand {
        return IncreaseCounterCommand(aggregateId, incrementValue)
    }

    override fun expected(): List<IDomainEvent> {
        val exception = IllegalStateException("Value 2147483647 not valid, it has to be >= 0 and < 2147483647")
        return listOf(
            DomainErrorEvent(
                aggregateId,
                exception
            )
        )
    }

    override fun expectedException(): Exception? {
        return null
    }

    override fun emptyAggregate(): (IIdentity) -> CounterAggregateRoot ={
        CounterAggregateRoot(it as CounterAggregateId)
    }

    override fun streamNameRoot(): String = "IncreaseCounterWrongTest"
}