package com.xiaobaotv.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.xiaobaotv.app.ui.home.HomeScreen
import com.xiaobaotv.app.ui.category.CategoryScreen
import com.xiaobaotv.app.ui.search.SearchScreen
import com.xiaobaotv.app.ui.profile.ProfileScreen

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val DETAIL = "detail/{vodId}"
    const val PLAYER = "player/{vodId}/{episodeNum}"

    fun detail(vodId: Int) = "detail/$vodId"
    fun player(vodId: Int, episodeNum: Int = 1) = "player/$vodId/$episodeNum"
}

@Composable
fun XiaobaoNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen()
        }
        composable(Routes.CATEGORY) {
            CategoryScreen()
        }
        composable(Routes.SEARCH) {
            SearchScreen()
        }
        composable(Routes.PROFILE) {
            ProfileScreen()
        }
    }
}
