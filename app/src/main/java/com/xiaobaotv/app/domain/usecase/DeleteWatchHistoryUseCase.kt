package com.xiaobaotv.app.domain.usecase

import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import javax.inject.Inject

class DeleteWatchHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke(vodId: Int) {
        repository.deleteWatchHistory(vodId)
    }
}
