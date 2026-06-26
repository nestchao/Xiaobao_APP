package com.xiaobaotv.app.data.local

import com.xiaobaotv.app.domain.model.VodContent
import com.xiaobaotv.app.domain.model.WatchHistoryItem

fun VodContentEntity.toDomain(): VodContent {
    return VodContent(
        id = id,
        name = name,
        pic = pic,
        actor = actor,
        director = director,
        area = area,
        year = year,
        content = content,
        remarks = remarks,
        score = score,
        typeId = typeId,
        typeName = typeName
    )
}

fun VodContent.toEntity(): VodContentEntity {
    return VodContentEntity(
        id = id,
        name = name,
        pic = pic,
        actor = actor,
        director = director,
        area = area,
        year = year,
        content = content,
        remarks = remarks,
        score = score,
        typeId = typeId,
        typeName = typeName
    )
}

fun WatchHistoryEntity.toDomain(): WatchHistoryItem {
    return WatchHistoryItem(
        vodId = vodId,
        name = name,
        pic = pic,
        sourceIndex = sourceIndex,
        sourceName = sourceName,
        episodeIndex = episodeIndex,
        episodeName = episodeName,
        positionMs = positionMs,
        durationMs = durationMs,
        lastWatchedAt = lastWatchedAt
    )
}

fun WatchHistoryItem.toEntity(): WatchHistoryEntity {
    return WatchHistoryEntity(
        vodId = vodId,
        name = name,
        pic = pic,
        sourceIndex = sourceIndex,
        sourceName = sourceName,
        episodeIndex = episodeIndex,
        episodeName = episodeName,
        positionMs = positionMs,
        durationMs = durationMs,
        lastWatchedAt = lastWatchedAt
    )
}
