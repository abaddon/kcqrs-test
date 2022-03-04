package io.github.abaddon.kcqrs.test.helpers.counter.projection

import io.github.abaddon.kcqrs.core.projections.IProjectionKey

class CounterProjectionKey(): IProjectionKey {
    override fun key(): String = "counter_projection"
}