package com.xiaobaotv.app.domain.usecase

import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import javax.inject.Inject

class SaveWatchHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke(item: WatchHistoryItem) {
        repository.saveWatchHistory(item)
    }
}
