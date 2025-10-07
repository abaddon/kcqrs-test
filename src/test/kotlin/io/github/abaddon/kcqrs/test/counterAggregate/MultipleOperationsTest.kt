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

/**
 * Test case with multiple events in the given() phase
 * This tests the aggregate behavior after processing several events
 */
class MultipleOperationsTest : KcqrsAggregateTestSpecification<CounterAggregateRoot>() {

    override val aggregateId = CounterAggregateId(UUID.randomUUID())
    private val initialValue = 10
    private val firstIncrement = 5
    private val secondIncrement = 3
    private val thirdIncrement = 7

    override fun given(): List<IDomainEvent> {
        return listOf(
            CounterInitialisedEvent(aggregateId, initialValue, 1),
            CounterIncreasedEvent(aggregateId, firstIncrement, 2),
            CounterIncreasedEvent(aggregateId, secondIncrement, 3)
        )
    }

    override fun `when`(): IncreaseCounterCommand {
        return IncreaseCounterCommand(aggregateId, thirdIncrement)
    }

    override fun expected(): List<IDomainEvent> {
        return listOf(
            CounterIncreasedEvent(aggregateId, thirdIncrement, 4)
        )
    }

    override fun expectedException(): Exception? {
        return null
    }

    override fun emptyAggregate(): (IIdentity) -> CounterAggregateRoot = {
        CounterAggregateRoot(it as CounterAggregateId)
    }

    override fun streamNameRoot(): String = "MultipleOperationsTest"

    override fun membersToIgnore(): List<String> {
        return listOf("header")
    }
}
