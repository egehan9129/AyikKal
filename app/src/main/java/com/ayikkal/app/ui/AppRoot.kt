package com.ayikkal.app.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ayikkal.app.MainActivity
import com.ayikkal.app.domain.TestMode
import com.ayikkal.app.system.Permissions
import com.ayikkal.app.ui.components.AyikTopBar
import com.ayikkal.app.ui.components.ShieldStatus
import com.ayikkal.app.ui.screens.OnboardingScreen
import com.ayikkal.app.ui.screens.PermissionsScreen
import com.ayikkal.app.ui.screens.ReportScreen
import com.ayikkal.app.ui.screens.SettingsScreen
import com.ayikkal.app.ui.screens.ShieldScreen
import com.ayikkal.app.ui.screens.TestHubScreen
import com.ayikkal.app.ui.screens.TestRunScreen
import com.ayikkal.app.ui.screens.TrackScreen
import com.ayikkal.app.ui.theme.Ak

object Routes {
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    const val TRACK = "track"
    const val TEST = "test"
    const val SHIELD = "shield"
    const val REPORT = "report"
    const val SETTINGS = "settings"
    const val TEST_RUN = "test_run/{mode}"
    fun testRun(mode: TestMode) = "test_run/${mode.name}"
}

private data class Tab(val route: String, val label: String, val subtitle: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.TRACK, "Takip", "Takip", Icons.Rounded.LocalBar),
    Tab(Routes.TEST, "Test", "Test", Icons.Rounded.Psychology),
    Tab(Routes.SHIELD, "Kalkan", "Kalkan", Icons.Rounded.Shield),
    Tab(Routes.REPORT, "Rapor", "Dün gece ne oldu?", Icons.Rounded.Insights),
    Tab(Routes.SETTINGS, "Ayarlar", "Ayarlar", Icons.Rounded.Settings),
)

fun NavHostController.goTab(route: String) = navigate(route) {
    popUpTo(Routes.TRACK) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun AppRoot(pendingDest: MutableState<String?>) {
    val vm: AppViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val context = LocalContext.current
    val resumeKey = rememberResumeKey()
    val accessibilityOn = remember(resumeKey) { Permissions.isAccessibilityEnabled(context) }
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tab = tabs.firstOrNull { it.route == route }

    val status = when {
        state.drunkActive -> ShieldStatus.DRUNK
        !state.setupDone || !accessibilityOn -> ShieldStatus.SETUP
        else -> ShieldStatus.READY
    }

    LaunchedEffect(pendingDest.value) {
        val dest = pendingDest.value ?: return@LaunchedEffect
        pendingDest.value = null
        if (!state.onboardingDone) return@LaunchedEffect
        when (dest) {
            MainActivity.DEST_TEST_RUN ->
                if (state.sessionActive && !state.drunkActive) nav.navigate(Routes.testRun(TestMode.PERIODIC)) { launchSingleTop = true }
                else nav.goTab(Routes.TEST)
            MainActivity.DEST_SHIELD -> nav.goTab(Routes.SHIELD)
            MainActivity.DEST_REPORT -> nav.goTab(Routes.REPORT)
            else -> nav.goTab(Routes.TRACK)
        }
    }

    Scaffold(
        containerColor = Ak.Bg,
        topBar = {
            if (tab != null) {
                Box(Modifier.statusBarsPadding()) {
                    AyikTopBar(tab.subtitle, status) { nav.goTab(Routes.SETTINGS) }
                }
            }
        },
        bottomBar = { if (tab != null) BottomBar(route) { nav.goTab(it) } },
    ) { padding ->
        NavHost(
            navController = nav,
            // Sabit tutulmalı: onboarding bitince değişirse NavHost grafı sıfırlanır ve yönlendirme kaybolur.
            startDestination = remember { if (state.onboardingDone) Routes.TRACK else Routes.ONBOARDING },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onDone = {
                    vm.completeOnboarding()
                    nav.navigate(Routes.SETTINGS) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                })
            }
            composable(Routes.PERMISSIONS) { PermissionsScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.TRACK) {
                TrackScreen(
                    vm = vm,
                    accessibilityOn = accessibilityOn,
                    onOpenTest = { nav.navigate(Routes.testRun(it)) },
                    onOpenTab = { nav.goTab(it) },
                    onOpenPermissions = { nav.navigate(Routes.PERMISSIONS) },
                )
            }
            composable(Routes.TEST) { TestHubScreen(vm = vm, onStart = { nav.navigate(Routes.testRun(it)) }) }
            composable(Routes.SHIELD) { ShieldScreen(vm = vm, onSoberTest = { nav.navigate(Routes.testRun(TestMode.SOBER_UP)) }) }
            composable(Routes.REPORT) { ReportScreen(vm = vm) }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    vm = vm,
                    onOpenPermissions = { nav.navigate(Routes.PERMISSIONS) },
                    onDone = {
                        vm.markSetupDone()
                        if (!accessibilityOn) nav.navigate(Routes.PERMISSIONS) else nav.goTab(Routes.TRACK)
                    },
                )
            }
            composable(
                Routes.TEST_RUN,
                arguments = listOf(navArgument("mode") { type = NavType.StringType }),
            ) { entry ->
                val mode = TestMode.valueOf(entry.arguments?.getString("mode") ?: TestMode.VOLUNTARY.name)
                TestRunScreen(
                    mode = mode,
                    onExit = { failedIntoLock ->
                        nav.popBackStack()
                        if (failedIntoLock) nav.goTab(Routes.SHIELD)
                    },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(current: String?, onSelect: (String) -> Unit) {
    Column(Modifier.background(Ak.Lowest)) {
        HorizontalDivider(color = Ak.Hairline)
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { t ->
                val selected = t.route == current
                val color = if (selected) Ak.Primary else Ak.Muted
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(remember { MutableInteractionSource() }, null) { onSelect(t.route) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Ak.Primary.copy(alpha = 0.14f) else androidx.compose.ui.graphics.Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Icon(t.icon, t.label, tint = color, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(t.label, style = MaterialTheme.typography.labelMedium, color = color)
                }
            }
        }
    }
}
