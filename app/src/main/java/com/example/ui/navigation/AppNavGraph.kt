package com.example.ui.navigation

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.ViewModelFactory
import com.example.ui.screens.bookmarks.BookmarksScreen
import com.example.ui.screens.bookmarks.BookmarksViewModel
import com.example.ui.screens.files.FilesScreen
import com.example.ui.screens.files.FilesViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.modules.ModulesScreen
import com.example.ui.screens.modules.ModulesViewModel
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.search.SearchViewModel
import com.example.ui.screens.viewer.FileViewerScreen
import com.example.ui.screens.viewer.ViewerViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    factory: ViewModelFactory,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { fadeIn() + slideInHorizontally { it / 4 } },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() + slideOutHorizontally { it / 4 } },
        modifier = modifier
    ) {
        composable("home") {
            val hvm: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                viewModel = hvm,
                onYearClick = { yearName, semester ->
                    navController.navigate("modules/${Uri.encode(yearName)}/$semester")
                },
                onSearchClick = { navController.navigate("search") },
                onBookmarksClick = { navController.navigate("bookmarks") }
            )
        }

        composable(
            route = "modules/{yearName}/{semester}",
            arguments = listOf(
                navArgument("yearName") { type = NavType.StringType },
                navArgument("semester") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val yearName = Uri.decode(backStackEntry.arguments?.getString("yearName") ?: "")
            val semester = backStackEntry.arguments?.getInt("semester") ?: 1
            val mvm: ModulesViewModel = viewModel(factory = factory)
            ModulesScreen(
                viewModel = mvm,
                yearName = yearName,
                semester = semester,
                onModuleClick = { yName, sem, modName ->
                    navController.navigate(
                        "files/${Uri.encode(yName)}/$sem/${Uri.encode(modName)}"
                    )
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "files/{yearName}/{semester}/{moduleName}",
            arguments = listOf(
                navArgument("yearName") { type = NavType.StringType },
                navArgument("semester") { type = NavType.IntType },
                navArgument("moduleName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val yearName = Uri.decode(backStackEntry.arguments?.getString("yearName") ?: "")
            val semester = backStackEntry.arguments?.getInt("semester") ?: 1
            val moduleName = Uri.decode(backStackEntry.arguments?.getString("moduleName") ?: "")
            val fvm: FilesViewModel = viewModel(factory = factory)
            FilesScreen(
                viewModel = fvm,
                yearName = yearName,
                semester = semester,
                moduleName = moduleName,
                onFileClick = { file ->
                    navController.navigate(
                        "viewer/${Uri.encode(file.url)}/${Uri.encode(file.name)}"
                    )
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "viewer/{url}/{name}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = Uri.decode(backStackEntry.arguments?.getString("url") ?: "")
            val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "")
            val vvm: ViewerViewModel = viewModel(factory = factory)
            FileViewerScreen(
                viewModel = vvm,
                url = url,
                fileName = name,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("search") {
            val svm: SearchViewModel = viewModel(factory = factory)
            SearchScreen(
                viewModel = svm,
                onFileClick = { file ->
                    navController.navigate(
                        "viewer/${Uri.encode(file.url)}/${Uri.encode(file.name)}"
                    )
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("bookmarks") {
            val bvm: BookmarksViewModel = viewModel(factory = factory)
            BookmarksScreen(
                viewModel = bvm,
                onFileClick = { file ->
                    navController.navigate(
                        "viewer/${Uri.encode(file.url)}/${Uri.encode(file.name)}"
                    )
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
