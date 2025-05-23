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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


abstract class KcqrsAggregateTestSpecification<TAggregate : IAggregate>() {

    @OptIn(ExperimentalCoroutinesApi::class)
    protected val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    abstract val aggregateId: IIdentity
    protected lateinit var repository: InMemoryEventStoreForTestRepository<TAggregate>

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
     * Class members to exclude from the comparison test
     * @return list of members name to exclude
     */
    open fun membersToIgnore(): List<String> = listOf()

    /**
     * AggregateCommandHandler that will be used during the test
     * The default AggregateCommandHandler is SimpleAggregateCommandHandler
     * @return The AggregateCommandHandler used
     */
    open fun onCommandHandler(): IAggregateCommandHandler<TAggregate> =
        SimpleAggregateCommandHandler<TAggregate>(repository, testDispatcher)

    @BeforeEach
    fun setup() {
        repository = InMemoryEventStoreForTestRepository<TAggregate>(
            streamNameRoot(),
            emptyAggregate(),
            testDispatcher
        )
    }

    @AfterEach
    fun tearDown() {
        repository.cleanup()
    }

    @Test
    fun checkBehaviour() = testScope.runTest {
        val givenEvents = given()
        repository.addEventsToStorage(aggregateId, givenEvents)
        val handler = onCommandHandler()

        try {
            handler.handle(`when`())
            val expected = expected()
            val published = repository.loadEventsFromStorage(aggregateId).minus(givenEvents)

            compareEvents(expected, published, membersToIgnore())

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
        fun compareEvents(expected: List<IDomainEvent>, published: List<IDomainEvent>, membersToIgnore: List<String>) {
            assertEquals(expected.count(), published.count(), "Different number of expected/published events.")
            val compareLogic = CompareLogic(
                CompareLogicConfig()
                    .addMemberToIgnore("messageId")
                    .addMembersToIgnore(membersToIgnore)

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