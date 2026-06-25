package com.xiaobaotv.app.data.cache

import kotlinx.coroutines.CompletableDeferred
import java.util.LinkedHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared in-memory cache for detail page HTML.
 * Both ContentRepositoryImpl and VideoRepositoryImpl use this
 * so we never fetch the same detail page twice.
 * Bounded to 10 entries (LRU eviction) to avoid memory pressure on low-end devices.
 *
 * Supports concurrent [getOrFetch] so parallel coroutines share a single fetch.
 */
@Singleton
class DetailPageCache @Inject constructor() {

    private val cache = object : LinkedHashMap<Int, String>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, String>?): Boolean {
            return size > MAX_ENTRIES
        }
    }
    private val pendingFetches = HashMap<Int, CompletableDeferred<String>>()

    @Synchronized
    fun get(vodId: Int): String? = cache[vodId]

    @Synchronized
    fun put(vodId: Int, html: String) { cache[vodId] = html }

    /**
     * Returns the cached HTML for [vodId], or fetches it via [fetcher].
     * If multiple coroutines call this concurrently for the same [vodId],
     * only the first executes [fetcher]; the rest await the same result.
     */
    suspend fun getOrFetch(vodId: Int, fetcher: suspend () -> String): String {
        // Fast path: already cached
        get(vodId)?.let { return it }

        val deferred = CompletableDeferred<String>()
        val shouldFetch: Boolean
        synchronized(this) {
            // Re-check under lock
            get(vodId)?.let { return it }
            val existing = pendingFetches.putIfAbsent(vodId, deferred)
            if (existing != null) {
                // Another coroutine is already fetching — await outside the lock
                shouldFetch = false
            } else {
                shouldFetch = true
            }
        }

        if (!shouldFetch) {
            return deferred.await()
        }

        // We are the fetching coroutine
        try {
            val html = fetcher()
            put(vodId, html)
            deferred.complete(html)
            return html
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
            throw e
        } finally {
            synchronized(this) { pendingFetches.remove(vodId) }
        }
    }

    @Synchronized
    fun remove(vodId: Int) { cache.remove(vodId) }

    @Synchronized
    fun clear() { cache.clear() }

    companion object {
        private const val MAX_ENTRIES = 10
    }
}
