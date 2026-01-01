/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2025 Kenny Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.connectbot.R
import org.connectbot.data.entity.Host
import org.connectbot.ui.navigation.NavArgs
import org.connectbot.ui.navigation.NavDestinations
import org.connectbot.ui.navigation.navigateSafely
import org.connectbot.ui.navigation.safePopBackStack
import org.connectbot.ui.screens.hosteditor.HostEditorScreen
import org.connectbot.ui.screens.hosts.HostsAdaptiveScreen
import org.connectbot.ui.screens.profiles.ProfileListScreen
import org.connectbot.ui.screens.pubkeylist.PubkeyListScreen
import org.connectbot.ui.screens.settings.SettingsScreen
import org.connectbot.ui.theme.ConnectBotTheme
import timber.log.Timber

/**
 * Top-level destinations for the adaptive navigation suite.
 */
enum class AdaptiveDestination {
    HOSTS,
    PUBLIC_KEYS,
    PROFILES,
    SETTINGS
}

/**
 * Adaptive ConnectBot app using NavigationSuiteScaffold for top-level navigation.
 * Automatically adapts navigation UI based on window size:
 * - Compact (phones): Bottom navigation bar
 * - Medium (small tablets): Navigation rail
 * - Expanded (large tablets/desktop): Navigation drawer
 */
@Composable
fun AdaptiveConnectBotApp(
    appUiState: AppUiState,
    navController: NavHostController,
    makingShortcut: Boolean,
    onRetryMigration: () -> Unit,
    onSelectShortcut: (Host) -> Unit,
    onNavigateToConsole: (Host) -> Unit,
    modifier: Modifier = Modifier
) {
    ConnectBotTheme {
        when (appUiState) {
            is AppUiState.Loading -> {
                LoadingScreen(modifier = modifier)
            }

            is AppUiState.MigrationInProgress -> {
                MigrationScreen(
                    uiState = MigrationUiState.InProgress(appUiState.state),
                    onRetry = onRetryMigration,
                    modifier = modifier
                )
            }

            is AppUiState.MigrationFailed -> {
                MigrationScreen(
                    uiState = MigrationUiState.Failed(
                        appUiState.error,
                        appUiState.debugLog
                    ),
                    onRetry = onRetryMigration,
                    modifier = modifier
                )
            }

            is AppUiState.Ready -> {
                CompositionLocalProvider(LocalTerminalManager provides appUiState.terminalManager) {
                    NavHost(
                        navController = navController,
                        startDestination = "adaptive_home",
                        modifier = modifier
                    ) {
                        composable("adaptive_home") {
                            AdaptiveNavigationContent(
                                navController = navController,
                                makingShortcut = makingShortcut,
                                onSelectShortcut = onSelectShortcut,
                                onNavigateToConsole = onNavigateToConsole
                            )
                        }

                        composable(
                            route = "${NavDestinations.HOST_EDITOR}?${NavArgs.HOST_ID}={${NavArgs.HOST_ID}}",
                            arguments = listOf(
                                navArgument(NavArgs.HOST_ID) {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) {
                            HostEditorScreen(
                                onNavigateBack = { navController.safePopBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdaptiveNavigationContent(
    navController: NavHostController,
    makingShortcut: Boolean,
    onSelectShortcut: (Host) -> Unit,
    onNavigateToConsole: (Host) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track selected destination - persists across configuration changes
    var selectedDestination by rememberSaveable { mutableStateOf(AdaptiveDestination.HOSTS) }
    var showNavigation by remember { mutableStateOf(true) }

    Timber.d("AdaptiveNavigationContent: showNavigation=$showNavigation, selectedDestination=$selectedDestination")

    if (showNavigation) {
        NavigationSuiteScaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationSuiteItems = {
                item(
                    selected = selectedDestination == AdaptiveDestination.HOSTS,
                    onClick = { selectedDestination = AdaptiveDestination.HOSTS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = stringResource(R.string.title_hosts)
                        )
                    },
                    label = { Text(stringResource(R.string.title_hosts)) }
                )

                item(
                    selected = selectedDestination == AdaptiveDestination.PUBLIC_KEYS,
                    onClick = { selectedDestination = AdaptiveDestination.PUBLIC_KEYS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = stringResource(R.string.title_pubkey_list)
                        )
                    },
                    label = { Text(stringResource(R.string.title_pubkey_list)) }
                )

                item(
                    selected = selectedDestination == AdaptiveDestination.PROFILES,
                    onClick = { selectedDestination = AdaptiveDestination.PROFILES },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = stringResource(R.string.profile_list_title)
                        )
                    },
                    label = { Text(stringResource(R.string.profile_list_title)) }
                )

                item(
                    selected = selectedDestination == AdaptiveDestination.SETTINGS,
                    onClick = { selectedDestination = AdaptiveDestination.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.title_settings)
                        )
                    },
                    label = { Text(stringResource(R.string.title_settings)) }
                )
            },
            modifier = modifier
        ) {
            DestinationContent(
                selectedDestination = selectedDestination,
                navController = navController,
                makingShortcut = makingShortcut,
                onSelectShortcut = onSelectShortcut,
                onNavigateToConsole = onNavigateToConsole,
                onNavigationVisibilityChange = { visible -> showNavigation = visible }
            )
        }
    } else {
        // No navigation - just show content directly
        DestinationContent(
            selectedDestination = selectedDestination,
            navController = navController,
            makingShortcut = makingShortcut,
            onSelectShortcut = onSelectShortcut,
            onNavigateToConsole = onNavigateToConsole,
            onNavigationVisibilityChange = { visible -> showNavigation = visible },
            modifier = modifier
        )
    }
}

/**
 * Content for the selected destination
 */
@Composable
private fun DestinationContent(
    selectedDestination: AdaptiveDestination,
    navController: NavHostController,
    makingShortcut: Boolean,
    onSelectShortcut: (Host) -> Unit,
    onNavigateToConsole: (Host) -> Unit,
    onNavigationVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    when (selectedDestination) {
        AdaptiveDestination.HOSTS -> HostsDestination(
            navController = navController,
            makingShortcut = makingShortcut,
            onSelectShortcut = onSelectShortcut,
            onNavigateToConsole = onNavigateToConsole,
            onNavigationVisibilityChange = { visible ->
                Timber.d("HostsDestination onNavigationVisibilityChange: visible=$visible")
                onNavigationVisibilityChange(visible)
            }
        )

        AdaptiveDestination.PUBLIC_KEYS -> PublicKeysDestination(navController = navController)

        AdaptiveDestination.PROFILES -> ProfilesDestination(navController = navController)

        AdaptiveDestination.SETTINGS -> SettingsDestination()
    }
}

/**
 * Hosts destination using adaptive layout with ListDetailPaneScaffold
 */
@Composable
private fun HostsDestination(
    navController: NavHostController,
    makingShortcut: Boolean,
    onSelectShortcut: (Host) -> Unit,
    onNavigateToConsole: (Host) -> Unit,
    onNavigationVisibilityChange: (Boolean) -> Unit = {}
) {
    HostsAdaptiveScreen(
        makingShortcut = makingShortcut,
        onSelectShortcut = onSelectShortcut,
        onNavigateToConsole = onNavigateToConsole,
        onNavigateToHostEditor = { hostId ->
            if (hostId != null) {
                navController.navigateSafely("${NavDestinations.HOST_EDITOR}?hostId=$hostId")
            } else {
                navController.navigateSafely(NavDestinations.HOST_EDITOR)
            }
        },
        onNavigationVisibilityChange = onNavigationVisibilityChange
    )
}

/**
 * Public Keys destination - will eventually be replaced with ListDetailPaneScaffold
 */
@Composable
private fun PublicKeysDestination(navController: NavHostController) {
    // TODO: Replace with PubkeysAdaptiveScreen using ListDetailPaneScaffold
    PubkeyListScreen(
        onNavigateBack = {
            // No back button in top-level destination
        },
        onNavigateToGenerate = {
            navController.navigate(NavDestinations.GENERATE_PUBKEY)
        },
        onNavigateToEdit = { pubkey ->
            navController.navigate("${NavDestinations.PUBKEY_EDITOR}/${pubkey.id}")
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Profiles destination - will eventually be replaced with ListDetailPaneScaffold
 */
@Composable
private fun ProfilesDestination(navController: NavHostController) {
    // TODO: Replace with ProfilesAdaptiveScreen using ListDetailPaneScaffold
    ProfileListScreen(
        onNavigateBack = {
            // No back button in top-level destination
        },
        onNavigateToEdit = { profile ->
            navController.navigate("${NavDestinations.PROFILE_EDITOR}/${profile.id}")
        }
    )
}

/**
 * Settings destination - single screen, no adaptive layout needed
 */
@Composable
private fun SettingsDestination() {
    SettingsScreen(
        onNavigateBack = {
            // No back button in top-level destination
        },
        modifier = Modifier.fillMaxSize()
    )
}
