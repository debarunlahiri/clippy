package com.debarunlahiri.clippy.ui.navigation

/** Navigation routes for the app */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: Long) = "item_detail/$itemId"
    }
}
