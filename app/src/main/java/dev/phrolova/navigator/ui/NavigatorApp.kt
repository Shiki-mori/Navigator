package dev.phrolova.navigator.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.phrolova.navigator.ui.calendar.CalendarScreen
import dev.phrolova.navigator.ui.calendar.CalendarViewModel
import dev.phrolova.navigator.ui.edit.EditDayScreen
import dev.phrolova.navigator.ui.edit.EditDayViewModel
import dev.phrolova.navigator.ui.home.HomeScreen
import dev.phrolova.navigator.ui.home.HomeViewModel
import dev.phrolova.navigator.ui.settings.SettingsScreen
import dev.phrolova.navigator.ui.settings.SettingsViewModel
import dev.phrolova.navigator.ui.stats.StatsScreen
import dev.phrolova.navigator.ui.stats.StatsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

private data class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    Tab("home", "首页", Icons.Outlined.Home),
    Tab("calendar", "月历", Icons.Outlined.CalendarMonth),
    Tab("stats", "统计", Icons.Outlined.BarChart),
    Tab("settings", "设置", Icons.Outlined.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigatorApp() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val container = LocalAppContainer.current
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route.orEmpty()
    val showBottomBar = tabs.any { it.route == route }
    val title = when {
        route.startsWith("edit") -> "编辑记录"
        route == "home" -> "打卡"
        route == "calendar" -> "月历"
        route == "stats" -> "统计"
        route == "settings" -> "设置"
        else -> "打卡"
    }
    val showMessage: (String) -> Unit = { text ->
        scope.launch { snackbarHostState.showSnackbar(text) }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val filled = container.autoCheckIn()
            if (filled.isNotEmpty()) {
                snackbarHostState.showSnackbar(autoCheckInMessage(filled))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (route.startsWith("edit")) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(container))
                HomeScreen(viewModel = vm, onMessage = showMessage)
            }
            composable("calendar") {
                val vm: CalendarViewModel = viewModel(factory = CalendarViewModel.factory(container))
                CalendarScreen(
                    viewModel = vm,
                    onOpenDay = { date ->
                        navController.navigate("edit/${date.toEpochDay()}")
                    },
                )
            }
            composable("stats") {
                val vm: StatsViewModel = viewModel(factory = StatsViewModel.factory(container))
                StatsScreen(viewModel = vm)
            }
            composable("settings") {
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
                SettingsScreen(viewModel = vm, onMessage = showMessage)
            }
            composable(
                route = "edit/{epochDay}",
                arguments = listOf(navArgument("epochDay") { type = NavType.LongType }),
            ) { entry ->
                val date = LocalDate.ofEpochDay(entry.arguments?.getLong("epochDay") ?: 0L)
                val vm: EditDayViewModel = viewModel(factory = EditDayViewModel.factory(container, date))
                EditDayScreen(viewModel = vm, onMessage = showMessage)
            }
        }
    }
}

internal fun autoCheckInMessage(dates: List<LocalDate>): String {
    return "已自动打卡：" + dates.joinToString("、")
}
