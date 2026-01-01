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

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.connectbot.R
import org.connectbot.data.HostRepository
import org.connectbot.data.entity.Host
import org.connectbot.di.CoroutineDispatchers
import org.connectbot.service.ServiceError
import org.connectbot.service.TerminalBridge
import org.connectbot.service.TerminalManager
import timber.log.Timber
import javax.inject.Inject

/**
 * Connection state for a host.
 */
enum class ConnectionState {
    UNKNOWN,
    CONNECTED,
    DISCONNECTED
}

/**
 * UI state for the Hosts adaptive screen.
 * Combines host list state and console/terminal state.
 */
data class HostsUiState(
    // Host list state
    val hosts: List<Host> = emptyList(),
    val connectionStates: Map<Long, ConnectionState> = emptyMap(),
    val sortedByColor: Boolean = false,

    // Terminal/console state
    val bridges: List<TerminalBridge> = emptyList(),
    val selectedHostId: Long? = null,
    val currentBridgeIndex: Int = 0,

    // Shared state
    val isLoading: Boolean = false,
    val error: String? = null,

    // Export/import state
    val exportedJson: String? = null,
    val exportResult: ExportResult? = null,
    val importResult: ImportResult? = null,

    // UI state for adaptive layout
    val isListPaneVisible: Boolean = true,
    val showQuickConnect: Boolean = false,

    // Revision counter for forcing recomposition
    val revision: Int = 0
)

data class ImportResult(
    val hostsImported: Int,
    val hostsSkipped: Int,
    val profilesImported: Int,
    val profilesSkipped: Int
)

data class ExportResult(
    val hostCount: Int,
    val profileCount: Int
)

/**
 * Unified ViewModel for the Hosts adaptive screen.
 * Combines functionality from HostListViewModel and ConsoleViewModel.
 */
@HiltViewModel
class HostsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @param:ApplicationContext private val context: Context,
    private val repository: HostRepository,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    companion object {
        private const val KEY_SELECTED_HOST_ID = "selectedHostId"
        private const val KEY_LIST_PANE_VISIBLE = "listPaneVisible"
    }

    private var terminalManager: TerminalManager? = null
    private val _uiState = MutableStateFlow(HostsUiState(isLoading = true))
    val uiState: StateFlow<HostsUiState> = _uiState.asStateFlow()

    init {
        // Restore selected host from saved state
        savedStateHandle.get<Long>(KEY_SELECTED_HOST_ID)?.let { hostId ->
            _uiState.update { it.copy(selectedHostId = hostId) }
        }

        // Restore list pane visibility from saved state
        savedStateHandle.get<Boolean>(KEY_LIST_PANE_VISIBLE)?.let { visible ->
            _uiState.update { it.copy(isListPaneVisible = visible) }
        }

        observeHosts()
    }

    fun setTerminalManager(manager: TerminalManager) {
        if (terminalManager != manager) {
            terminalManager = manager

            // Observe bridges flow from TerminalManager
            viewModelScope.launch {
                manager.bridgesFlow.collect { bridges ->
                    updateBridges(bridges)
                    subscribeToActiveBridgeBells(bridges)
                }
            }

            // Observe host status changes
            observeHostStatusChanges()

            // Collect service errors
            collectServiceErrors()

            // Update initial connection states
            updateConnectionStates(_uiState.value.hosts)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeHosts() {
        viewModelScope.launch {
            _uiState
                .map { it.sortedByColor }
                .distinctUntilChanged()
                .flatMapLatest { sortedByColor ->
                    if (sortedByColor) {
                        repository.observeHostsSortedByColor()
                    } else {
                        repository.observeHosts()
                    }
                }
                .collect { hosts ->
                    updateConnectionStates(hosts)
                    _uiState.update {
                        it.copy(hosts = hosts, isLoading = false, error = null)
                    }
                }
        }
    }

    private fun observeHostStatusChanges() {
        val manager = terminalManager ?: return
        viewModelScope.launch {
            manager.hostStatusChangedFlow.collect {
                // Update connection states when terminal manager notifies us of changes
                updateConnectionStates(_uiState.value.hosts)
            }
        }
    }

    private fun collectServiceErrors() {
        val manager = terminalManager ?: return
        viewModelScope.launch {
            manager.serviceErrors.collect { error ->
                val errorMessage = formatServiceError(error)
                _uiState.update { it.copy(error = errorMessage) }
            }
        }
    }

    private fun subscribeToActiveBridgeBells(bridges: List<TerminalBridge>) {
        viewModelScope.launch {
            bridges.forEach { bridge ->
                launch {
                    bridge.bellEvents.collect {
                        val currentIndex = _uiState.value.currentBridgeIndex
                        val currentBridge = _uiState.value.bridges.getOrNull(currentIndex)

                        if (currentBridge == bridge) {
                            // The bridge is visible, play the beep
                            terminalManager?.playBeep()
                        } else {
                            // The bridge is not visible, send a notification
                            bridge.host.let {
                                terminalManager?.sendActivityNotification(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateBridges(allBridges: List<TerminalBridge>) {
        Timber.d("updateBridges called with ${allBridges.size} bridges: ${allBridges.map { it.host.id }}")
        _uiState.update { currentState ->
            // Find the bridge for the currently selected host, if any
            val selectedBridge = currentState.selectedHostId?.let { hostId ->
                allBridges.find { bridge -> bridge.host.id == hostId }
            }
            Timber.d("updateBridges: selectedHostId=${currentState.selectedHostId}, selectedBridge=${selectedBridge?.host?.id}")

            // If we have a selected bridge, set the current index to it
            val newIndex = if (selectedBridge != null) {
                allBridges.indexOf(selectedBridge).coerceAtLeast(0)
            } else if (currentState.currentBridgeIndex >= allBridges.size) {
                (allBridges.size - 1).coerceAtLeast(0)
            } else {
                currentState.currentBridgeIndex
            }

            currentState.copy(
                bridges = allBridges,
                currentBridgeIndex = newIndex,
                error = null
            )
        }
    }

    private fun formatServiceError(error: ServiceError): String = when (error) {
        is ServiceError.KeyLoadFailed -> {
            context.getString(R.string.error_key_load_failed, error.keyName, error.reason)
        }

        is ServiceError.ConnectionFailed -> {
            context.getString(
                R.string.error_connection_failed,
                error.hostNickname,
                error.hostname,
                error.reason
            )
        }

        is ServiceError.PortForwardLoadFailed -> {
            context.getString(
                R.string.error_port_forward_load_failed,
                error.hostNickname,
                error.reason
            )
        }

        is ServiceError.HostSaveFailed -> {
            context.getString(R.string.error_host_save_failed, error.hostNickname, error.reason)
        }

        is ServiceError.ColorSchemeLoadFailed -> {
            context.getString(R.string.error_color_scheme_load_failed, error.reason)
        }
    }

    private fun updateConnectionStates(hosts: List<Host>) {
        val states = hosts.associate { host ->
            host.id to getConnectionState(host)
        }
        _uiState.update { it.copy(connectionStates = states) }
    }

    private fun getConnectionState(host: Host): ConnectionState {
        val manager = terminalManager ?: return ConnectionState.UNKNOWN

        // Check if connected by ID
        if (manager.bridgesFlow.value.any { it.host.id == host.id }) {
            return ConnectionState.CONNECTED
        }

        // Check if in disconnected list by comparing ID
        if (manager.disconnectedFlow.value.any { it.id == host.id }) {
            return ConnectionState.DISCONNECTED
        }

        return ConnectionState.UNKNOWN
    }

    // Host list actions
    fun toggleSortOrder() {
        _uiState.update { it.copy(sortedByColor = !it.sortedByColor) }
    }

    fun deleteHost(host: Host) {
        viewModelScope.launch {
            try {
                repository.deleteHost(host)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete host")
                }
            }
        }
    }

    fun forgetHostKeys(host: Host) {
        viewModelScope.launch {
            try {
                repository.deleteKnownHostsForHost(host.id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to forget host keys")
                }
            }
        }
    }

    fun disconnectAll() {
        terminalManager?.disconnectAll(immediate = true, excludeLocal = false)
    }

    fun disconnectHost(host: Host) {
        val bridge = terminalManager?.bridgesFlow?.value?.find { it.host.id == host.id }
        bridge?.dispatchDisconnect(true)
    }

    // Console/terminal actions
    fun selectHost(hostId: Long?) {
        Timber.d("selectHost called: hostId=$hostId")
        if (hostId == null) {
            Timber.d("Clearing host selection")
            savedStateHandle.remove<Long>(KEY_SELECTED_HOST_ID)
            _uiState.update { it.copy(selectedHostId = null) }
            return
        }

        savedStateHandle[KEY_SELECTED_HOST_ID] = hostId
        _uiState.update { it.copy(selectedHostId = hostId) }

        // Find the bridge for this host and switch to it
        val bridges = _uiState.value.bridges
        val bridgeIndex = bridges.indexOfFirst { it.host.id == hostId }
        Timber.d("selectHost: found bridge at index $bridgeIndex for hostId=$hostId")
        if (bridgeIndex >= 0) {
            selectBridge(bridgeIndex)
        }
    }

    fun selectBridge(index: Int) {
        if (index in _uiState.value.bridges.indices) {
            _uiState.update { it.copy(currentBridgeIndex = index) }
        }
    }

    fun connectToHost(host: Host) {
        Timber.d("connectToHost called: hostId=${host.id}, nickname=${host.nickname}")
        viewModelScope.launch {
            try {
                val bridge = withContext(dispatchers.io) {
                    // Check if already connected
                    val existingBridge = terminalManager?.bridgesFlow?.value?.find { it.host.id == host.id }
                    Timber.d("connectToHost: existingBridge=${existingBridge?.host?.id}")
                    existingBridge ?: terminalManager?.openConnectionForHostId(host.id)
                }

                if (bridge != null) {
                    Timber.d("connectToHost: bridge created/found, calling selectHost(${host.id})")
                    selectHost(host.id)
                } else {
                    Timber.w("connectToHost: bridge is null for hostId=${host.id}")
                    _uiState.update {
                        it.copy(error = "Failed to connect to ${host.nickname}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "connectToHost: exception for hostId=${host.id}")
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to connect")
                }
            }
        }
    }

    // Export/import actions
    fun exportHosts() {
        viewModelScope.launch {
            try {
                val (json, exportCounts) = withContext(dispatchers.io) {
                    repository.exportHostsToJson()
                }
                val exportResult = ExportResult(
                    hostCount = exportCounts.hostCount,
                    profileCount = exportCounts.profileCount
                )
                _uiState.update { it.copy(exportedJson = json, exportResult = exportResult) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to export hosts")
                }
            }
        }
    }

    fun clearExportedJson() {
        _uiState.update { it.copy(exportedJson = null, exportResult = null) }
    }

    fun importHosts(jsonString: String) {
        viewModelScope.launch {
            try {
                val importCounts = withContext(dispatchers.io) {
                    repository.importHostsFromJson(jsonString)
                }
                val importResult = ImportResult(
                    hostsImported = importCounts.hostsImported,
                    hostsSkipped = importCounts.hostsSkipped,
                    profilesImported = importCounts.profilesImported,
                    profilesSkipped = importCounts.profilesSkipped
                )
                _uiState.update { it.copy(importResult = importResult) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to import hosts")
                }
            }
        }
    }

    fun clearImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }

    // UI state actions
    fun setListPaneVisible(visible: Boolean) {
        savedStateHandle[KEY_LIST_PANE_VISIBLE] = visible
        _uiState.update { it.copy(isListPaneVisible = visible) }
    }

    fun showQuickConnect() {
        _uiState.update { it.copy(showQuickConnect = true) }
    }

    fun hideQuickConnect() {
        _uiState.update { it.copy(showQuickConnect = false) }
    }

    fun quickConnect(uriString: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val host = parseQuickConnectUri(uriString)
                val uri = host.getUri()
                val bridge = terminalManager?.openConnection(uri)
                if (bridge != null) {
                    withContext(dispatchers.main) {
                        selectHost(bridge.host.id)
                    }
                } else {
                    withContext(dispatchers.main) {
                        _uiState.update { it.copy(error = "Failed to connect to $uriString") }
                    }
                }
            } catch (e: Exception) {
                withContext(dispatchers.main) {
                    _uiState.update { it.copy(error = "Invalid URI: ${e.message}") }
                }
            }
        }
    }

    private fun parseQuickConnectUri(uriString: String): Host {
        // Parse URI in format: [protocol://][user@]host[:port]
        // Examples:
        //   root@192.168.1.1:22
        //   ssh://user@example.com
        //   telnet://192.168.1.1:23

        val trimmed = uriString.trim()
        var protocol = "ssh"
        var remaining = trimmed

        // Extract protocol if present
        if (remaining.contains("://")) {
            val parts = remaining.split("://", limit = 2)
            protocol = parts[0].lowercase()
            remaining = parts[1]
        }

        // Parse [user@]host[:port]
        var username = ""
        var hostname = ""
        var port = when (protocol) {
            "ssh" -> 22
            "telnet" -> 23
            else -> 22
        }

        // Extract username if present
        if (remaining.contains("@")) {
            val parts = remaining.split("@", limit = 2)
            username = parts[0]
            remaining = parts[1]
        }

        // Extract port if present
        if (remaining.contains(":")) {
            val parts = remaining.split(":", limit = 2)
            hostname = parts[0]
            port = parts[1].toIntOrNull() ?: throw IllegalArgumentException("Invalid port number")
        } else {
            hostname = remaining
        }

        if (hostname.isBlank()) {
            throw IllegalArgumentException("Hostname is required")
        }

        // Create temporary host (negative ID means it won't be saved)
        return Host(
            id = -System.currentTimeMillis(), // Negative ID for temporary hosts
            nickname = "$username@$hostname:$port",
            protocol = protocol,
            username = username,
            hostname = hostname,
            port = port
        )
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refreshMenuState() {
        _uiState.update { it.copy(revision = it.revision + 1) }
    }
}
