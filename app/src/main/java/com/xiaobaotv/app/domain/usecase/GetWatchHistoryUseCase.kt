package com.xiaobaotv.app.domain.usecase

import com.xiaobaotv.app.domain.model.WatchHistoryItem
import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWatchHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    operator fun invoke(): Flow<List<WatchHistoryItem>> {
        return repository.getWatchHistory()
    }
}
