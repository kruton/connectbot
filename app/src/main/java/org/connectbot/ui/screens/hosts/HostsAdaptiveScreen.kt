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

package org.connectbot.ui.screens.hosts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.connectbot.R
import org.connectbot.data.entity.Host
import org.connectbot.terminal.Terminal
import org.connectbot.ui.LocalTerminalManager
import org.connectbot.ui.components.InlinePrompt
import org.connectbot.util.rememberTerminalTypefaceResultFromStoredValue
import timber.log.Timber

/**
 * Adaptive Hosts screen using ListDetailPaneScaffold.
 * Shows host list in one pane and terminal/details in another.
 * Adapts between single-pane (phones) and dual-pane (tablets) layouts.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HostsAdaptiveScreen(
    modifier: Modifier = Modifier,
    makingShortcut: Boolean = false,
    onSelectShortcut: (Host) -> Unit = {},
    onNavigateToConsole: (Host) -> Unit = {},
    onNavigateToHostEditor: (Long?) -> Unit = {},
    onNavigationVisibilityChange: (Boolean) -> Unit = {},
    viewModel: HostsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val terminalManager = LocalTerminalManager.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Set terminal manager when available
    LaunchedEffect(terminalManager) {
        terminalManager?.let { viewModel.setTerminalManager(it) }
    }

    // Show errors in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<Long>()
    val scope = rememberCoroutineScope()
    var hostRailExpanded by remember { mutableStateOf(false) }

    // Navigate to detail pane only when the bridge for the selected host exists.
    // This prevents premature navigation during connection establishment.
    LaunchedEffect(uiState.selectedHostId, uiState.bridges) {
        uiState.selectedHostId?.let { hostId ->
            val bridgeExists = uiState.bridges.any { it.host.id == hostId }
            Timber.d("Navigation check: hostId=$hostId, bridgeExists=$bridgeExists, bridges=${uiState.bridges.map { it.host.id }}")
            if (bridgeExists) {
                Timber.d("Navigating to detail pane for hostId=$hostId")
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, hostId)
            } else {
                Timber.d("NOT navigating yet - bridge doesn't exist for hostId=$hostId")
            }
        }
    }

    // In phone layout, navigate back when all hosts disconnect
    // Only navigate back if we're actually on the detail pane
    LaunchedEffect(uiState.bridges.isEmpty(), navigator.canNavigateBack(), navigator.currentDestination?.pane) {
        val isOnDetailPane = navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail
        Timber.d("Auto-navigate-back check: canNavigateBack=${navigator.canNavigateBack()}, bridgesEmpty=${uiState.bridges.isEmpty()}, selectedHostId=${uiState.selectedHostId}, isOnDetailPane=$isOnDetailPane")
        if (navigator.canNavigateBack() &&
            uiState.bridges.isEmpty() &&
            uiState.selectedHostId != null &&
            isOnDetailPane
        ) {
            Timber.d("Auto-navigating back to list")
            scope.launch {
                navigator.navigateBack()
            }
        }
    }

    // Clear selectedHostId when on the list pane in phone mode.
    // This allows re-selecting the same host to navigate to detail pane again.
    LaunchedEffect(navigator.currentDestination?.pane, uiState.selectedHostId) {
        val currentPane = navigator.currentDestination?.pane
        val isOnListPane = currentPane == ListDetailPaneScaffoldRole.List

        Timber.d("Clear selection check: currentPane=$currentPane, isOnListPane=$isOnListPane, selectedHostId=${uiState.selectedHostId}")

        // When on the list pane, clear selection so the same host can be selected again
        if (isOnListPane && uiState.selectedHostId != null) {
            Timber.d("Clearing selectedHostId because we're on the list pane")
            viewModel.selectHost(null)
        }
    }

    // Notify parent about navigation visibility based on phone mode and detail pane visibility.
    // Only hide navigation when we're actually showing a bridge in the detail pane,
    // not while waiting for a connection to establish.
    val canNavigateBack = navigator.canNavigateBack()
    val currentPane = navigator.currentDestination?.pane
    val hasBridge = uiState.bridges.isNotEmpty()
    val shouldHideNavigation = canNavigateBack &&
        currentPane == ListDetailPaneScaffoldRole.Detail &&
        hasBridge

    // Capture current callback to avoid restarting effect when it changes
    val currentOnNavigationVisibilityChange by rememberUpdatedState(onNavigationVisibilityChange)

    // Use snapshotFlow to only trigger when the actual value changes, not on every recomposition
    LaunchedEffect(Unit) {
        snapshotFlow { shouldHideNavigation }
            .distinctUntilChanged()
            .collect { shouldHide ->
                Timber.d("Navigation visibility CHANGED: shouldHide=$shouldHide (canNavigateBack=$canNavigateBack, currentPane=$currentPane, hasBridge=$hasBridge)")
                Timber.d("Calling onNavigationVisibilityChange(!shouldHide=${!shouldHide})")
                currentOnNavigationVisibilityChange(!shouldHide)
            }
    }

    // In dual-pane mode with connected host, use collapsible rail instead of list pane
    // Only use rail if we're actually in expanded mode (dual-pane capable)
    val isConnected = uiState.selectedHostId != null && uiState.bridges.isNotEmpty()
    val isExpandedMode = navigator.scaffoldDirective.maxHorizontalPartitions > 1
    val shouldUseRail = !navigator.canNavigateBack() && isConnected && isExpandedMode

    // Single top-level emitter wrapping both layout modes
    Box(modifier = modifier.fillMaxSize()) {
        if (shouldUseRail) {
            // Dual-pane mode: collapsible rail overlaying full-screen terminal
            Box(modifier = Modifier.fillMaxSize()) {
                // Full-width terminal with session tabs in TopAppBar
                HostDetailPane(
                    bridges = uiState.bridges,
                    currentBridgeIndex = uiState.currentBridgeIndex,
                    onBridgeSelect = { index ->
                        viewModel.selectBridge(index)
                        hostRailExpanded = false
                    },
                    canNavigateBack = false,
                    onToggleHostList = { hostRailExpanded = !hostRailExpanded },
                    onNavigateBack = {},
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (hostRailExpanded) {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    hostRailExpanded = false
                                }
                            } else {
                                Modifier
                            }
                        )
                )

                // Collapsible host list rail overlaying on the left
                AnimatedVisibility(
                    visible = hostRailExpanded,
                    enter = expandHorizontally(
                        animationSpec = tween(300),
                        expandFrom = Alignment.Start
                    ),
                    exit = shrinkHorizontally(
                        animationSpec = tween(300),
                        shrinkTowards = Alignment.Start
                    ),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Surface(
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shadowElevation = 8.dp
                    ) {
                        HostListPane(
                            hosts = uiState.hosts,
                            connectionStates = uiState.connectionStates,
                            sortedByColor = uiState.sortedByColor,
                            makingShortcut = makingShortcut,
                            onHostClick = { host ->
                                hostRailExpanded = false
                                if (makingShortcut) {
                                    onSelectShortcut(host)
                                } else {
                                    viewModel.selectHost(host.id)
                                }
                            },
                            onHostConnect = { host ->
                                Timber.d("onHostConnect called for host: ${host.nickname} (id=${host.id})")
                                hostRailExpanded = false
                                viewModel.connectToHost(host)
                            },
                            onToggleSortOrder = { viewModel.toggleSortOrder() },
                            onShowQuickConnect = {
                                hostRailExpanded = false
                                viewModel.showQuickConnect()
                            },
                            onNavigateToHostEditor = { hostId ->
                                hostRailExpanded = false
                                onNavigateToHostEditor(hostId)
                            },
                            onDeleteHost = { host -> viewModel.deleteHost(host) },
                            onDisconnectHost = { host -> viewModel.disconnectHost(host) },
                            onForgetHostKeys = { host -> viewModel.forgetHostKeys(host) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            // Single-pane mode or no connection: use standard ListDetailPaneScaffold
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    AnimatedPane {
                        HostListPane(
                            hosts = uiState.hosts,
                            connectionStates = uiState.connectionStates,
                            sortedByColor = uiState.sortedByColor,
                            makingShortcut = makingShortcut,
                            onHostClick = { host ->
                                if (makingShortcut) {
                                    onSelectShortcut(host)
                                } else {
                                    viewModel.selectHost(host.id)
                                }
                            },
                            onHostConnect = { host ->
                                Timber.d("onHostConnect called for host: ${host.nickname} (id=${host.id})")
                                viewModel.connectToHost(host)
                            },
                            onToggleSortOrder = { viewModel.toggleSortOrder() },
                            onShowQuickConnect = { viewModel.showQuickConnect() },
                            onNavigateToHostEditor = onNavigateToHostEditor,
                            onDeleteHost = { host -> viewModel.deleteHost(host) },
                            onDisconnectHost = { host -> viewModel.disconnectHost(host) },
                            onForgetHostKeys = { host -> viewModel.forgetHostKeys(host) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        if (uiState.selectedHostId != null && uiState.bridges.isNotEmpty()) {
                            HostDetailPane(
                                bridges = uiState.bridges,
                                currentBridgeIndex = uiState.currentBridgeIndex,
                                onBridgeSelect = { index -> viewModel.selectBridge(index) },
                                canNavigateBack = navigator.canNavigateBack(),
                                onToggleHostList = null,
                                onNavigateBack = {
                                    scope.launch {
                                        navigator.navigateBack()
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            EmptyDetailPane(
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Show snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }

    // Quick connect bottom sheet
    if (uiState.showQuickConnect) {
        QuickConnectBottomSheet(
            onDismiss = { viewModel.hideQuickConnect() },
            onConnect = { uriString ->
                viewModel.quickConnect(uriString)
                viewModel.hideQuickConnect()
            }
        )
    }
}

/**
 * List pane showing all hosts.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HostListPane(
    hosts: List<Host>,
    connectionStates: Map<Long, ConnectionState>,
    sortedByColor: Boolean,
    makingShortcut: Boolean,
    onHostClick: (Host) -> Unit,
    onHostConnect: (Host) -> Unit,
    onToggleSortOrder: () -> Unit,
    onShowQuickConnect: () -> Unit,
    onNavigateToHostEditor: (Long?) -> Unit,
    onDeleteHost: (Host) -> Unit,
    onDisconnectHost: (Host) -> Unit,
    onForgetHostKeys: (Host) -> Unit,
    modifier: Modifier = Modifier
) {
    var fabMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (!makingShortcut) {
                FloatingActionButtonMenu(
                    expanded = fabMenuExpanded,
                    button = {
                        ToggleFloatingActionButton(
                            checked = fabMenuExpanded,
                            onCheckedChange = { fabMenuExpanded = !fabMenuExpanded }
                        ) {
                            Icon(
                                painter = rememberVectorPainter(
                                    if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                                ),
                                contentDescription = if (fabMenuExpanded) {
                                    stringResource(android.R.string.cancel)
                                } else {
                                    stringResource(R.string.quick_connect)
                                },
                                modifier = Modifier.animateIcon({ checkedProgress })
                            )
                        }
                    }
                ) {
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabMenuExpanded = false
                            onShowQuickConnect()
                        },
                        icon = { Icon(Icons.Default.FlashOn, contentDescription = null) },
                        text = { Text(stringResource(R.string.quick_connect)) }
                    )
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabMenuExpanded = false
                            onNavigateToHostEditor(null)
                        },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text(stringResource(R.string.hostpref_add_host)) }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(hosts, key = { it.id }) { host ->
                HostListItem(
                    host = host,
                    connectionState = connectionStates[host.id] ?: ConnectionState.UNKNOWN,
                    onClick = {
                        val state = connectionStates[host.id]
                        if (state == ConnectionState.CONNECTED) {
                            onHostClick(host)
                        } else {
                            onHostConnect(host)
                        }
                    },
                    onEdit = { onNavigateToHostEditor(host.id) },
                    onDelete = { onDeleteHost(host) },
                    onDisconnect = { onDisconnectHost(host) },
                    onForgetHostKeys = { onForgetHostKeys(host) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (hosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.list_hosts_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual host list item.
 */
@Composable
private fun HostListItem(
    host: Host,
    connectionState: ConnectionState,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDisconnect: () -> Unit,
    onForgetHostKeys: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(
                text = host.nickname,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${host.username}@${host.hostname}:${host.port}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            // Connection indicator
            if (connectionState == ConnectionState.CONNECTED) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = stringResource(R.string.connected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(12.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
        },
        trailingContent = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.button_more_options))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    // Edit option
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.list_host_edit)) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )

                    // Disconnect option (only if connected)
                    if (connectionState == ConnectionState.CONNECTED) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.list_host_disconnect)) },
                            onClick = {
                                showMenu = false
                                onDisconnect()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LinkOff, contentDescription = null)
                            }
                        )
                    }

                    // Forget host keys option
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.list_host_forget_keys)) },
                        onClick = {
                            showMenu = false
                            onForgetHostKeys()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.VpnKey, contentDescription = null)
                        }
                    )

                    // Delete option
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.list_host_delete)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        },
        modifier = modifier.clickable(onClick = onClick)
    )
}

/**
 * Detail pane showing terminal for connected host.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HostDetailPane(
    bridges: List<org.connectbot.service.TerminalBridge>,
    currentBridgeIndex: Int,
    onBridgeSelect: (Int) -> Unit,
    canNavigateBack: Boolean,
    onToggleHostList: (() -> Unit)?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentBridge = bridges.getOrNull(currentBridgeIndex)
    val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }
    val fontResult = rememberTerminalTypefaceResultFromStoredValue(currentBridge?.fontFamily)
    val fontSize = remember { prefs.getInt("fontsize", 10) }
    val termFocusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            // Show custom top app bar with navigation button and session selector
            if (canNavigateBack) {
                // Single-pane mode: back button + session tabs (no rail menu)
                HostSelectorTopAppBar(
                    bridges = bridges,
                    currentBridgeIndex = currentBridgeIndex,
                    onBridgeSelect = onBridgeSelect,
                    onNavigateBack = onNavigateBack,
                    useMenuIcon = false
                )
            } else if (onToggleHostList != null) {
                // Dual-pane mode: menu button + session tabs (with rail menu)
                HostSelectorTopAppBar(
                    bridges = bridges,
                    currentBridgeIndex = currentBridgeIndex,
                    onBridgeSelect = onBridgeSelect,
                    onNavigateBack = onToggleHostList,
                    useMenuIcon = true
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Terminal view with slide animation
            AnimatedContent(
                targetState = currentBridgeIndex,
                transitionSpec = {
                    val animationSpec = tween<androidx.compose.ui.unit.IntOffset>(durationMillis = 200)
                    if (targetState > initialState) {
                        // Sliding left (next tab)
                        slideInHorizontally(animationSpec) { width -> width } togetherWith
                            slideOutHorizontally(animationSpec) { width -> -width }
                    } else {
                        // Sliding right (previous tab)
                        slideInHorizontally(animationSpec) { width -> -width } togetherWith
                            slideOutHorizontally(animationSpec) { width -> width }
                    }
                },
                label = "terminal_slide",
                modifier = Modifier.fillMaxSize()
            ) { bridgeIndex ->
                val bridge = bridges.getOrNull(bridgeIndex)
                if (bridge != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Terminal(
                            terminalEmulator = bridge.terminalEmulator,
                            modifier = Modifier.fillMaxSize(),
                            typeface = fontResult.typeface,
                            initialFontSize = fontSize.sp,
                            keyboardEnabled = true,
                            showSoftKeyboard = false,
                            focusRequester = termFocusRequester,
                            forcedSize = null,
                            modifierManager = bridge.keyHandler,
                            onTerminalTap = {},
                            onImeVisibilityChanged = {}
                        )

                        // Show inline prompts from the current bridge (non-modal at bottom)
                        val promptState by bridge.promptManager.promptState.collectAsState()

                        InlinePrompt(
                            promptRequest = promptState,
                            onResponse = { response ->
                                bridge.promptManager.respond(response)
                            },
                            onCancel = {
                                bridge.promptManager.cancelPrompt()
                            },
                            onDismissed = {
                                termFocusRequester.requestFocus()
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom TopAppBar that combines navigation button with horizontal scrollable host selector.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HostSelectorTopAppBar(
    bridges: List<org.connectbot.service.TerminalBridge>,
    currentBridgeIndex: Int,
    onBridgeSelect: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    useMenuIcon: Boolean = false
) {
    TopAppBar(
        title = {
            // Horizontal scrollable selector for hosts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bridges.forEachIndexed { index, bridge ->
                    // Card-style selector for each host
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (index == currentBridgeIndex) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onBridgeSelect(index) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = bridge.host.nickname,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (index == currentBridgeIndex) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = if (useMenuIcon) Icons.Default.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (useMenuIcon) stringResource(R.string.title_hosts) else null
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

/**
 * Empty state for detail pane when no host is selected.
 */
@Composable
private fun EmptyDetailPane(
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.select_host_to_connect),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Quick connect modal bottom sheet for entering connection URIs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickConnectBottomSheet(
    onDismiss: () -> Unit,
    onConnect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var uriText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_connect),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = stringResource(R.string.host_editor_quick_connect_example),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = uriText,
                onValueChange = {
                    uriText = it
                    errorText = null
                },
                label = { Text(stringResource(R.string.host_editor_quick_connect_label)) },
                placeholder = { Text(stringResource(R.string.host_editor_quick_connect_placeholder)) },
                isError = errorText != null,
                supportingText = errorText?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (uriText.isNotBlank()) {
                            scope.launch {
                                sheetState.hide()
                                onConnect(uriText)
                            }
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (uriText.isBlank()) {
                            errorText = "Please enter a connection URI"
                            return@Button
                        }
                        scope.launch {
                            sheetState.hide()
                            onConnect(uriText)
                        }
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    }
}
