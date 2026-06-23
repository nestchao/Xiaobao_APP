package com.xiaobaotv.app.data.model

import com.xiaobaotv.app.domain.model.VodContent

fun VodItem.toDomain(): VodContent {
    return VodContent(
        id = vodId,
        name = vodName,
        pic = if (vodPic.startsWith("http")) vodPic else "https://www.xiaobaotv.tv$vodPic",
        actor = vodActor,
        director = vodDirector,
        area = vodArea,
        year = vodYear,
        content = vodContent,
        remarks = vodRemarks,
        score = vodScore,
        typeId = type?.typeId ?: 0,
        typeName = type?.typeName
    )
}
