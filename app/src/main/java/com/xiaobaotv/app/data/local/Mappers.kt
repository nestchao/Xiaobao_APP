package com.xiaobaotv.app.data.local

import com.xiaobaotv.app.domain.model.VodContent

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
