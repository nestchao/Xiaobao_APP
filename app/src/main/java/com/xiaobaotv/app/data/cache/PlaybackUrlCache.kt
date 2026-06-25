package com.xiaobaotv.app.data.cache

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared in-memory cache for pre-fetched playback URLs.
 * DetailViewModel pre-loads the first episode's URL so
 * PlayerViewModel can start playback without a network round-trip.
 */
@Singleton
class PlaybackUrlCache @Inject constructor() {
    private val cache = LinkedHashMap<Int, String>(16, 0.75f, true)

    @Synchronized
    fun get(vodId: Int): String? = cache[vodId]

    @Synchronized
    fun put(vodId: Int, url: String) { cache[vodId] = url }

    @Synchronized
    fun remove(vodId: Int) { cache.remove(vodId) }

    @Synchronized
    fun clear() { cache.clear() }
}
