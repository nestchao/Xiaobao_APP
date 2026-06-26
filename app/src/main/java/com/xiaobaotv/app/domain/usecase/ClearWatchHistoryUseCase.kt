package com.xiaobaotv.app.domain.usecase

import com.xiaobaotv.app.domain.repository.WatchHistoryRepository
import javax.inject.Inject

class ClearWatchHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke() {
        repository.clearAll()
    }
}
