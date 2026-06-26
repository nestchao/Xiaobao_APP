package com.xiaobaotv.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.xiaobaotv.app.ui.home.HomeScreen
import com.xiaobaotv.app.ui.category.CategoryScreen
import com.xiaobaotv.app.ui.search.SearchScreen
import com.xiaobaotv.app.ui.history.HistoryScreen
import com.xiaobaotv.app.ui.player.PlayerScreen
import com.xiaobaotv.app.ui.detail.DetailScreen

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category"
    const val SEARCH = "search"
    const val HISTORY = "history"
    const val DETAIL = "detail/{vodId}"
    const val PLAYER = "player/{vodId}/{episodeNum}"

    fun detail(vodId: Int) = "detail/$vodId"
    fun player(vodId: Int, episodeNum: Int = 1) = "player/$vodId/$episodeNum"
}

@Composable
fun XiaobaoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(150)) },
        exitTransition = { fadeOut(animationSpec = tween(150)) },
        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
        popExitTransition = { fadeOut(animationSpec = tween(150)) }
    ) {
        composable(Routes.HOME) {
            HomeScreen(onVodClick = { vodId -> navController.navigate(Routes.detail(vodId)) })
        }
        composable(Routes.CATEGORY) {
            CategoryScreen(onVodClick = { vodId -> navController.navigate(Routes.detail(vodId)) })
        }
        composable(Routes.SEARCH) {
            SearchScreen(onVodClick = { vodId -> navController.navigate(Routes.detail(vodId)) })
        }
        composable(Routes.HISTORY) {
            HistoryScreen(onVodClick = { vodId -> navController.navigate(Routes.detail(vodId)) })
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("vodId") { type = NavType.IntType })
        ) { backStackEntry ->
            val vodId = backStackEntry.arguments?.getInt("vodId") ?: 0
            DetailScreen(
                vodId = vodId,
                onPlayClick = { id, ep -> navController.navigate(Routes.player(id, ep)) },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("vodId") { type = NavType.IntType },
                navArgument("episodeNum") { type = NavType.IntType; defaultValue = 1 }
            )
        ) { backStackEntry ->
            val vodId = backStackEntry.arguments?.getInt("vodId") ?: 0
            val episodeNum = backStackEntry.arguments?.getInt("episodeNum") ?: 1
            PlayerScreen(
                vodId = vodId,
                episodeIndex = episodeNum - 1,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
