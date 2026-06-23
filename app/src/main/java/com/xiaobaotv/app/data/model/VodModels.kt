package com.xiaobaotv.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VodListResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "msg") val msg: String,
    @Json(name = "page") val page: Int,
    @Json(name = "pagecount") val pageCount: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "total") val total: Int,
    @Json(name = "list") val list: List<VodItem>
)

@JsonClass(generateAdapter = true)
data class VodItem(
    @Json(name = "vod_id") val vodId: Int,
    @Json(name = "vod_name") val vodName: String,
    @Json(name = "vod_pic") val vodPic: String,
    @Json(name = "vod_actor") val vodActor: String?,
    @Json(name = "vod_director") val vodDirector: String?,
    @Json(name = "vod_area") val vodArea: String?,
    @Json(name = "vod_year") val vodYear: String?,
    @Json(name = "vod_content") val vodContent: String?,
    @Json(name = "vod_remarks") val vodRemarks: String?,
    @Json(name = "vod_score") val vodScore: String?,
    @Json(name = "type") val type: VodType?
)

@JsonClass(generateAdapter = true)
data class VodType(
    @Json(name = "type_id") val typeId: Int,
    @Json(name = "type_name") val typeName: String,
    @Json(name = "type_pid") val typePid: Int
)
