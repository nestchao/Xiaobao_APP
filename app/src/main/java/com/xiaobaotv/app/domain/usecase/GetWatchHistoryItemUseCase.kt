package com.xiaobaotv.app.domain.usecase

import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import javax.inject.Inject

class GetWatchHistoryItemUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke(vodId: Int): WatchHistoryItem? {
        return repository.getWatchHistoryItem(vodId)
    }
}
