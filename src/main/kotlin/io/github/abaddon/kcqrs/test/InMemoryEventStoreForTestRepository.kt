package io.github.abaddon.kcqrs.test

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.EventStoreRepository
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class InMemoryEventStoreForTestRepository<TAggregate : IAggregate>(
    private val _streamNameRoot: String,
    private val _emptyAggregate: (aggregateId: IIdentity) -> TAggregate,
    coroutineContext: CoroutineContext
) : EventStoreRepository<TAggregate>(coroutineContext) {

    private val storage = mutableMapOf<String, MutableList<IDomainEvent>>()
    private val projectionHandlers = mutableListOf<IProjectionHandler<*>>()

    override fun aggregateIdStreamName(aggregateId: IIdentity): String =
        "${_streamNameRoot}.${aggregateId.valueAsString()}"

    /**
     * This method should be used only for testing purpose.
     * It allows saving events directly to the Events store without using the aggregate
     */
    suspend fun addEventsToStorage(aggregateId: IIdentity, events: List<IDomainEvent>) = withContext(coroutineContext) {
        persist(aggregateIdStreamName(aggregateId), events, mapOf(), 0)
    }

    /**
     * This method should be used only for testing purpose.
     * It allows getting events directly from the Events store
     */
    suspend fun loadEventsFromStorage(aggregateId: IIdentity): Result<List<IDomainEvent>> =
        withContext(coroutineContext) {
            runCatching {
                loadEvents(aggregateIdStreamName(aggregateId))
                    .getOrThrow()
                    .toList()
            }
        }


    override suspend fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    ): Result<Unit> = withContext(coroutineContext) {
        runCatching {
            val currentEvents = storage.getOrDefault(streamName, listOf()).toMutableList()
            currentEvents.addAll(uncommittedEvents.toMutableList())
            storage[streamName] = currentEvents
        }
    }

    override suspend fun loadEvents(streamName: String, startFrom: Long): Result<Flow<IDomainEvent>> =
        withContext(coroutineContext) {
            runCatching {
                storage.getOrDefault(streamName, listOf()).asFlow()
            }
        }

    override suspend fun <TProjection : IProjection> subscribe(projectionHandler: IProjectionHandler<TProjection>) {
        projectionHandlers.add(projectionHandler)
    }

    override fun emptyAggregate(aggregateId: IIdentity): TAggregate = _emptyAggregate(aggregateId)

    override suspend fun publish(events: List<IDomainEvent>): Result<Unit> =
        withContext(coroutineContext) {
            runCatching {
                projectionHandlers.forEach { projectionHandlers -> projectionHandlers.onEvents(events) }
            }
        }
}