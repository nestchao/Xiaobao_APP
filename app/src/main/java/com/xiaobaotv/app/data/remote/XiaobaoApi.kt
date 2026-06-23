package com.xiaobaotv.app.data.remote

import com.xiaobaotv.app.data.model.VodListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface XiaobaoApi {

    @GET("index.php/ajax/data")
    suspend fun getVodList(
        @Query("mid") mid: Int? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("wd") wd: String? = null,
        @Query("tid") tid: Int? = null,
        @Query("ids") ids: String? = null
    ): VodListResponse
}
