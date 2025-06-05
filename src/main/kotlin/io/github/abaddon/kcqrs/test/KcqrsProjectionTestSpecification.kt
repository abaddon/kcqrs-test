package io.github.abaddon.kcqrs.test

import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.helpers.flatMap
import io.github.abaddon.kcqrs.core.persistence.InMemoryProjectionRepository
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler
import io.github.abaddon.kcqrs.core.projections.IProjectionKey
import io.github.abaddon.kcqrs.core.projections.SimpleProjectionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class KcqrsProjectionTestSpecification<TProjection : IProjection> {

    @OptIn(ExperimentalCoroutinesApi::class)
    protected val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
    protected val testScope = TestScope(testDispatcher)

    //private val eventRepository: MutableList<IDomainEvent> = mutableListOf()
    private val projectionRepository: InMemoryProjectionRepository<TProjection> =
        InMemoryProjectionRepository<TProjection>(testScope.coroutineContext, emptyProjection())
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
        SimpleProjectionHandler<TProjection>(projectionRepository, projectionKey, testScope.coroutineContext)

    @Test
    fun checkBehaviour() = testScope.runTest {
        val projectionHandler = onProjectionHandler()
        //Apply the initial events
        val result = projectionHandler.onEvents(given())
            .flatMap {
                projectionHandler.onEvent(`when`())
            }.flatMap {
                projectionRepository.getByKey(projectionKey)
            }.flatMap { actualProjection ->
                val expectedProjection = expected()
                runCatching {
                    compareProjection(expectedProjection, actualProjection)
                }
            }
        when {
            result.isFailure -> {
                val actualException = result.exceptionOrNull()!!
                if (expectedException == null) {
                    assertTrue(
                        false,
                        "${actualException.javaClass.simpleName}: ${actualException.message}\n${actualException.stackTraceToString()} "
                    )
                } else {
                    assertEquals(
                        actualException.javaClass.simpleName,
                        expectedException?.javaClass?.simpleName,
                        "Exception type  ${actualException.javaClass.simpleName} differs from expected type ${expectedException?.javaClass?.simpleName}"
                    )
                    assertEquals(
                        actualException.message,
                        expectedException?.message,
                        "Exception message  ${actualException.message} differs from expected type ${actualException.message}"
                    )
                }
            }
        }
    }

    companion object {

        fun <TProjection : IProjection> compareProjection(expected: TProjection, actual: TProjection) {
            assertEquals(expected, actual)
        }
    }
}
