package io.github.abaddon.kcqrs.test

import io.github.abaddon.kcqrs.core.IAggregate
import io.github.abaddon.kcqrs.core.IIdentity
import io.github.abaddon.kcqrs.core.domain.messages.events.IDomainEvent
import io.github.abaddon.kcqrs.core.persistence.EventStoreRepository
import io.github.abaddon.kcqrs.core.projections.IProjection
import io.github.abaddon.kcqrs.core.projections.IProjectionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InMemoryEventStoreForTestRepository<TAggregate : IAggregate>(
    private val _streamNameRoot: String,
    private val _emptyAggregate: (aggregateId: IIdentity) -> TAggregate,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : EventStoreRepository<TAggregate>(dispatcher) {

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
    suspend fun loadEventsFromStorage(aggregateId: IIdentity): List<IDomainEvent> = withContext(coroutineContext) {
        load(aggregateIdStreamName(aggregateId))
    }


    override suspend fun persist(
        streamName: String,
        uncommittedEvents: List<IDomainEvent>,
        header: Map<String, String>,
        currentVersion: Long
    ): Result<Unit> = withContext(coroutineContext) {
        val currentEvents = storage.getOrDefault(streamName, listOf()).toMutableList()
        currentEvents.addAll(uncommittedEvents.toMutableList())
        storage[streamName] = currentEvents
        Result.success(Unit)
    }

    override suspend fun load(streamName: String, startFrom: Long): List<IDomainEvent> = withContext(coroutineContext) {
        storage.getOrDefault(streamName, listOf())
    }

    override suspend fun <TProjection : IProjection> subscribe(projectionHandler: IProjectionHandler<TProjection>) {
        projectionHandlers.add(projectionHandler)
    }

    override fun emptyAggregate(aggregateId: IIdentity): TAggregate = _emptyAggregate(aggregateId)

    override suspend fun publish(persistResult: Result<Unit>, events: List<IDomainEvent>): Result<Unit> =
        withContext(coroutineContext) {
            persistResult
                .onSuccess {
                    projectionHandlers.forEach { projectionHandlers -> projectionHandlers.onEvents(events) }
                    Result.success(Unit)
                }
                .onFailure {
                    persistResult
                }
        }
}