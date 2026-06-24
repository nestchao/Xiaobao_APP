package com.xiaobaotv.app.data.cache

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared in-memory cache for detail page HTML.
 * Both ContentRepositoryImpl and VideoRepositoryImpl use this
 * so we never fetch the same detail page twice.
 */
@Singleton
class DetailPageCache @Inject constructor() {
    private val cache = mutableMapOf<Int, String>()

    @Synchronized
    fun get(vodId: Int): String? = cache[vodId]

    @Synchronized
    fun put(vodId: Int, html: String) { cache[vodId] = html }

    @Synchronized
    fun remove(vodId: Int) { cache.remove(vodId) }

    @Synchronized
    fun clear() { cache.clear() }
}
