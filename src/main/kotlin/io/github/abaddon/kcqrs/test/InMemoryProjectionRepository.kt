package io.github.abaddon.kcqrs.test

//class InMemoryProjectionRepository<TProjection : IProjection>(
//    val funEmpty: (key: IProjectionKey) -> TProjection
//) : IProjectionRepository<TProjection> {
//
//    private val inMemoryStorage = mutableMapOf<IProjectionKey, TProjection>()
//    var offsetStorage: Long = 0
//
//
//    override suspend fun getByKey(key: IProjectionKey): TProjection? {
//        return inMemoryStorage[key]
//    }
//
//    override suspend fun save(projection: TProjection, offset: Long) {
//        inMemoryStorage[projection.key] = projection
//        offsetStorage = if (offset > offsetStorage) offset else offsetStorage
//    }
//
//    override fun emptyProjection(key: IProjectionKey): TProjection = funEmpty(key)
//
//}