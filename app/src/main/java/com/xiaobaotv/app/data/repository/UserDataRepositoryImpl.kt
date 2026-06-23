package com.xiaobaotv.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.xiaobaotv.app.domain.model.WatchlistItem
import com.xiaobaotv.app.domain.repository.UserDataRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserDataRepository {

    override fun getWatchlist(userId: String): Flow<List<WatchlistItem>> = callbackFlow {
        val ref = firestore.collection("users")
            .document(userId)
            .collection("watchlist")

        val listener = ref.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { doc ->
                    WatchlistItem(
                        vodId = doc.getLong("vodId")?.toInt() ?: return@mapNotNull null,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        pic = doc.getString("pic") ?: "",
                        remarks = doc.getString("remarks"),
                        type = doc.getString("type"),
                        addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()
                    )
                }
                trySend(items)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addToWatchlist(userId: String, item: WatchlistItem) {
        val data = hashMapOf(
            "vodId" to item.vodId.toLong(),
            "name" to item.name,
            "pic" to item.pic,
            "remarks" to item.remarks,
            "type" to item.type,
            "addedAt" to item.addedAt
        )
        firestore.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(item.vodId.toString())
            .set(data)
            .await()
    }

    override suspend fun removeFromWatchlist(userId: String, vodId: Int) {
        firestore.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(vodId.toString())
            .delete()
            .await()
    }

    override suspend fun isInWatchlist(userId: String, vodId: Int): Boolean {
        val doc = firestore.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(vodId.toString())
            .get()
            .await()
        return doc.exists()
    }
}