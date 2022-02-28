package io.github.abaddon.kcqrs.test

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.InMemoryProjectionRepository
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler
import io.github.abaddon.kcqrs.core.projections.IProjectionKey
import io.github.abaddon.kcqrs.core.projections.SimpleProjectionHandler
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class KcqrsProjectionTestSpecification<TProjection : IProjection> {

    private val eventRepository: MutableList<IDomainEvent> = mutableListOf()
    private val projectionRepository: InMemoryProjectionRepository<TProjection> =
        InMemoryProjectionRepository<TProjection>(emptyProjection())
    private val expectedException: Exception? = expectedException();

    abstract val projectionKey: IProjectionKey

    abstract fun expectedException(): Exception?

    abstract fun emptyProjection(): (key: IProjectionKey) -> TProjection

    /**
     * List of events needed to initialise the aggregate
     * @return list of DomainEvent.
     */
    abstract fun given(): List<IDomainEvent>

    /**
     * Event that we want to test
     * @return The command to test
     */
    abstract fun `when`(): IDomainEvent

    /**
     * Expected events after the execution of the command
     * @return list of DomainEvent
     */
    abstract fun expected(): TProjection

    /**
     * onProjectionHandler that will be used during the test
     * The default ProjectionHandler is SimpleProjectionHandler
     * @return The ProjectionHandler used
     */
    open fun onProjectionHandler(): IProjectionHandler<TProjection> =
        SimpleProjectionHandler<TProjection>(projectionRepository, projectionKey)

    @Test
    fun checkBehaviour() {
        val projectionHandler = onProjectionHandler()
        //Apply the initial events
        projectionHandler.onEvents(given())
        //Apply the event to test
        projectionHandler.onEvent(`when`())

        val expectedProjection = expected()

        val actualProjection = runBlocking {
            projectionRepository.getByKey(projectionKey)
        }

        try {
            compareProjection(expectedProjection, actualProjection)

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

        fun <TProjection : IProjection> compareProjection(expected: TProjection, actual: TProjection) {
            assertEquals(expected, actual)
        }
    }
}