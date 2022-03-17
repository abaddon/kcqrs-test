package io.github.abaddon.kcqrs.test

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.IAggregateCommandHandler
import io.github.abaddon.kcqrs.core.domain.SimpleAggregateCommandHandler
import io.github.abaddon.kcqrs.core.domain.messages.commands.ICommand
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.InMemoryEventStoreRepository
import io.github.abaddon.kustomCompare.CompareLogic
import io.github.abaddon.kustomCompare.config.CompareLogicConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class KcqrsAggregateTestSpecification<TAggregate : IAggregate>() {

    abstract val aggregateId: IIdentity
    open val eventRepository: InMemoryEventStoreRepository<TAggregate> = InMemoryEventStoreRepository<TAggregate>(
        streamNameRoot(),
        emptyAggregate()
    )
    open val expectedException: Exception? = expectedException();

    abstract fun expectedException(): Exception?

    abstract fun emptyAggregate(): (identity: IIdentity) -> TAggregate

    abstract fun streamNameRoot(): String

    /**
     * List of events needed to initialise the aggregate
     * @return list of DomainEvent.
     */
    abstract fun given(): List<IDomainEvent>

    /**
     * Command that we want to test
     * @return The command to test
     */
    abstract fun `when`(): ICommand<TAggregate>

    /**
     * Expected events after the execution of the command
     * @return list of DomainEvent
     */
    abstract fun expected(): List<IDomainEvent>

    /**
     * AggregateCommandHandler that will be used during the test
     * The default AggregateCommandHandler is SimpleAggregateCommandHandler
     * @return The AggregateCommandHandler used
     */
    open fun onCommandHandler(): IAggregateCommandHandler<TAggregate> =
        SimpleAggregateCommandHandler<TAggregate>(eventRepository)

    @Test
    fun checkBehaviour() {
        val givenEvents = given()
        eventRepository.addEventsToStorage(aggregateId, givenEvents)
        var handler = onCommandHandler()

        try {
            runBlocking {
                handler.handle(`when`())
            }
            val expected = expected()
            val published = eventRepository.loadEventsFromStorage(aggregateId).minus(givenEvents)

            compareEvents(expected, published)

        } catch (e: Exception) {
            if (expectedException == null)
                assertTrue(false, "${e.javaClass.simpleName}: ${e.message}\n${e.stackTraceToString()} ")
            assertEquals(
                e.javaClass.simpleName,
                expectedException?.javaClass?.simpleName,
                "Exception type  ${e.javaClass.simpleName} differs from expected type ${expectedException?.javaClass?.simpleName}"
            )
            assertEquals(
                e.message,
                expectedException?.message,
                "Exception message  ${e.message} differs from expected type ${e.message}"
            )
        }

    }

    companion object {
        fun compareEvents(expected: List<IDomainEvent>, published: List<IDomainEvent>) {
            assertEquals(expected.count(), published.count(), "Different number of expected/published events.")
            val compareLogic = CompareLogic(
                CompareLogicConfig()
                    .addMemberToIgnore("messageId")
            )
            val eventPairs = expected.zip(published) { e, p -> mapOf(Pair("expected", e), Pair("published", p)) }
            eventPairs.forEach { eventPair ->

                val result = compareLogic.compare(eventPair["expected"]!!, eventPair["published"]!!)
                assertTrue(
                    result.result(),
                    "Events ${eventPair["expected"]} and ${eventPair["published"]} are different"
                )
            }
        }
    }
}