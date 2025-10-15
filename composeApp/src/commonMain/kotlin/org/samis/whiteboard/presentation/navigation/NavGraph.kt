package org.samis.whiteboard.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.compose.viewmodel.koinViewModel
import org.samis.whiteboard.presentation.dashboard.DashboardScreen
import org.samis.whiteboard.presentation.dashboard.DashboardViewModel
import org.samis.whiteboard.presentation.settings.SettingsScreen
import org.samis.whiteboard.presentation.settings.SettingsViewModel
import org.samis.whiteboard.presentation.whiteboard.WhiteboardScreen
import org.samis.whiteboard.presentation.whiteboard.WhiteboardViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {

    NavHost(
        modifier = Modifier.padding(innerPadding),
        navController = navController,
        startDestination = Routes.DashboardScreen
    ) {
        composable<Routes.DashboardScreen> {
            val viewModel = koinViewModel<DashboardViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            DashboardScreen(
                state = state,
                viewModel::onEvent,
                onSettingsIconClick = { navController.navigate(Routes.SettingsScreen) },
                onCardClick = { whiteboardId ->
                    navController.navigate(Routes.WhiteboardScreen(whiteboardId = whiteboardId))
                },
                onAddNewWhiteboardClick = {
                    navController.navigate(Routes.WhiteboardScreen(whiteboardId = null))
                }
            )
        }
        composable<Routes.WhiteboardScreen> {
            val viewModel = koinViewModel<WhiteboardViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            WhiteboardScreen(
                state = state,
                onEvent = viewModel::onEvent,
                navController = navController,
                onHomeIconClick = { navController.navigateUp() }
            )
        }
        composable<Routes.SettingsScreen> {
            val viewModel = koinViewModel<SettingsViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            SettingsScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBackIconClick = { navController.navigateUp() }
            )
        }
    }
}