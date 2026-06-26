package com.xiaobaotv.app.data.cache

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared in-memory cache for pre-fetched playback URLs.
 * DetailViewModel pre-loads the first episode's URL so
 * PlayerViewModel can start playback without a network round-trip.
 * Bounded to 5 entries (LRU eviction) to avoid memory pressure.
 */
@Singleton
class PlaybackUrlCache @Inject constructor() {
    private val cache = object : LinkedHashMap<String, String>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > MAX_ENTRIES
        }
    }

    @Synchronized
    fun get(vodId: Int, episodeIndex: Int): String? = cache["$vodId-$episodeIndex"]

    @Synchronized
    fun put(vodId: Int, episodeIndex: Int, url: String) { cache["$vodId-$episodeIndex"] = url }

    @Synchronized
    fun remove(vodId: Int, episodeIndex: Int) { cache.remove("$vodId-$episodeIndex") }

    @Synchronized
    fun clear() { cache.clear() }

    companion object {
        private const val MAX_ENTRIES = 5
    }
}
