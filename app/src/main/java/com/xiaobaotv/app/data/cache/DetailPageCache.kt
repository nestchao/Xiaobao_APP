package com.xiaobaotv.app.data.cache

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared in-memory cache for detail page HTML.
 * Both ContentRepositoryImpl and VideoRepositoryImpl use this
 * so we never fetch the same detail page twice.
 */
@Singleton
class DetailPageCache @Inject constructor() {
    private val cache = ConcurrentHashMap<Int, String>()

    fun get(vodId: Int): String? = cache[vodId]

    fun put(vodId: Int, html: String) { cache[vodId] = html }

    fun remove(vodId: Int) { cache.remove(vodId) }

    fun clear() { cache.clear() }
}
