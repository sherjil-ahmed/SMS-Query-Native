package com.example.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ConversationThread
import com.example.data.model.DatePreset
import com.example.data.model.FilterDirection
import com.example.data.model.QueryFilter
import com.example.data.model.QueryMatcher
import com.example.data.model.SimCard
import com.example.data.model.SmsMessage
import com.example.data.model.SortOption
import com.example.data.model.TimeWindowPreset
import com.example.data.repository.SmsRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SmsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository(application)

    val threads: StateFlow<List<ConversationThread>> = repository.conversationThreads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<SmsMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Query Filter state
    private val _queryFilter = MutableStateFlow(QueryFilter())
    val queryFilter: StateFlow<QueryFilter> = _queryFilter.asStateFlow()

    // Dynamic Filtered Query Results list
    val queryResults: StateFlow<List<QueryMatcher.FilterResult>> = combine(allMessages, _queryFilter) { msgs, filter ->
        QueryMatcher.evaluate(msgs, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected conversation thread for detail screen
    private val _selectedAddress = MutableStateFlow<String?>(null)
    val selectedAddress: StateFlow<String?> = _selectedAddress.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeConversationMessages: StateFlow<List<SmsMessage>> = _selectedAddress.flatMapLatest { address ->
        if (address == null) flowOf(emptyList())
        else repository.getMessagesForAddress(address)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dialog & Navigation States
    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isComposeDialogOpen = MutableStateFlow(false)
    val isComposeDialogOpen: StateFlow<Boolean> = _isComposeDialogOpen.asStateFlow()

    private val _isSimulateDialogOpen = MutableStateFlow(false)
    val isSimulateDialogOpen: StateFlow<Boolean> = _isSimulateDialogOpen.asStateFlow()

    private val _currentScreen = MutableStateFlow(Screen.INBOX)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // SMS Runtime Permission states
    private val _isSmsPermissionGranted = MutableStateFlow(
        ContextCompat.checkSelfPermission(application, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    )
    val isSmsPermissionGranted: StateFlow<Boolean> = _isSmsPermissionGranted.asStateFlow()

    private val _showPermissionRationaleDialog = MutableStateFlow(false)
    val showPermissionRationaleDialog: StateFlow<Boolean> = _showPermissionRationaleDialog.asStateFlow()

    private val _isPermissionPermanentlyDenied = MutableStateFlow(false)
    val isPermissionPermanentlyDenied: StateFlow<Boolean> = _isPermissionPermanentlyDenied.asStateFlow()

    private val _permissionRequestTrigger = MutableStateFlow<Long?>(null)
    val permissionRequestTrigger: StateFlow<Long?> = _permissionRequestTrigger.asStateFlow()

    // Default SMS App Role State
    private val _isDefaultSmsApp = MutableStateFlow(com.example.util.DefaultSmsAppUtil.isDefaultSmsApp(application))
    val isDefaultSmsApp: StateFlow<Boolean> = _isDefaultSmsApp.asStateFlow()

    private val _isDefaultSmsDialogOpen = MutableStateFlow(false)
    val isDefaultSmsDialogOpen: StateFlow<Boolean> = _isDefaultSmsDialogOpen.asStateFlow()

    private val _requestDefaultSmsAppTrigger = MutableStateFlow<Long?>(null)
    val requestDefaultSmsAppTrigger: StateFlow<Long?> = _requestDefaultSmsAppTrigger.asStateFlow()

    // Dual SIM management
    private val _availableSimCards = MutableStateFlow<List<SimCard>>(SimCard.getAvailableSims(application))
    val availableSimCards: StateFlow<List<SimCard>> = _availableSimCards.asStateFlow()

    private val _selectedSimSlot = MutableStateFlow(SimCard.getDefaultSimSlot(application))
    val selectedSimSlot: StateFlow<Int> = _selectedSimSlot.asStateFlow()

    val selectedSimCard: StateFlow<SimCard?> = combine(_availableSimCards, _selectedSimSlot) { sims, slot ->
        sims.find { it.slotIndex == slot } ?: sims.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isSimSettingsDialogOpen = MutableStateFlow(false)
    val isSimSettingsDialogOpen: StateFlow<Boolean> = _isSimSettingsDialogOpen.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            checkPermissionStatus()
            checkDefaultSmsAppStatus()
            refreshSimCards()
        }
    }

    enum class Screen {
        INBOX,
        QUERY_RESULTS,
        CONVERSATION
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectConversation(address: String) {
        _selectedAddress.value = address
        _currentScreen.value = Screen.CONVERSATION
        viewModelScope.launch {
            repository.markThreadAsRead(address)
        }
    }

    fun closeConversation() {
        _selectedAddress.value = null
        _currentScreen.value = Screen.INBOX
    }

    fun updateFilter(filter: QueryFilter) {
        _queryFilter.value = filter
    }

    fun resetFilter() {
        _queryFilter.value = QueryFilter()
    }

    fun setKeywords(keywords: String) {
        _queryFilter.value = _queryFilter.value.copy(keywords = keywords)
    }

    fun setSenderQuery(senderQuery: String) {
        _queryFilter.value = _queryFilter.value.copy(senderQuery = senderQuery)
    }

    fun setDirection(direction: FilterDirection) {
        _queryFilter.value = _queryFilter.value.copy(direction = direction)
    }

    fun setDatePreset(preset: DatePreset) {
        _queryFilter.value = _queryFilter.value.copy(datePreset = preset)
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _queryFilter.value = _queryFilter.value.copy(
            datePreset = DatePreset.CUSTOM_RANGE,
            customStartDateMillis = startMillis,
            customEndDateMillis = endMillis
        )
    }

    fun toggleTimeOfDayWindow(enabled: Boolean) {
        _queryFilter.value = _queryFilter.value.copy(applyTimeOfDayWindow = enabled)
    }

    fun setTimePreset(preset: TimeWindowPreset) {
        if (preset == TimeWindowPreset.CUSTOM) {
            _queryFilter.value = _queryFilter.value.copy(
                timePreset = preset,
                applyTimeOfDayWindow = true
            )
        } else {
            _queryFilter.value = _queryFilter.value.copy(
                timePreset = preset,
                timeStartHour = preset.startHour,
                timeStartMinute = preset.startMinute,
                timeEndHour = preset.endHour,
                timeEndMinute = preset.endMinute,
                applyTimeOfDayWindow = true
            )
        }
    }

    fun setCustomTimeWindow(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        _queryFilter.value = _queryFilter.value.copy(
            timePreset = TimeWindowPreset.CUSTOM,
            timeStartHour = startHour,
            timeStartMinute = startMinute,
            timeEndHour = endHour,
            timeEndMinute = endMinute,
            applyTimeOfDayWindow = true
        )
    }

    fun setSortBy(sort: SortOption) {
        _queryFilter.value = _queryFilter.value.copy(sortBy = sort)
    }

    fun setComposeDialogOpen(isOpen: Boolean) {
        _isComposeDialogOpen.value = isOpen
    }

    fun setSimulateDialogOpen(isOpen: Boolean) {
        _isSimulateDialogOpen.value = isOpen
    }

    fun toggleTheme() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun setSimSettingsDialogOpen(isOpen: Boolean) {
        _isSimSettingsDialogOpen.value = isOpen
    }

    fun selectSimSlot(slot: Int) {
        _selectedSimSlot.value = slot
        val sim = _availableSimCards.value.find { it.slotIndex == slot }
        val label = sim?.shortLabel ?: "SIM $slot"
        _snackbarMessage.value = "Sending SIM changed to SIM $slot • $label"
    }

    fun refreshSimCards() {
        val updatedSims = SimCard.getAvailableSims(getApplication())
        _availableSimCards.value = updatedSims
        if (updatedSims.none { it.slotIndex == _selectedSimSlot.value }) {
            val defaultSlot = updatedSims.find { it.isDefaultSms }?.slotIndex ?: updatedSims.firstOrNull()?.slotIndex ?: 1
            _selectedSimSlot.value = defaultSlot
        }
    }

    fun sendMessage(address: String, senderName: String, body: String, simSlot: Int? = null) {
        if (body.isBlank() || address.isBlank()) return
        val targetSlot = simSlot ?: _selectedSimSlot.value
        val simCard = _availableSimCards.value.find { it.slotIndex == targetSlot }
        val subId = simCard?.subscriptionId
        viewModelScope.launch {
            val result = repository.sendMessage(
                address = address.trim(),
                senderName = senderName.trim(),
                body = body.trim(),
                simSlot = targetSlot,
                subscriptionId = subId
            )
            if (result.isSuccess) {
                val label = simCard?.shortLabel ?: "SIM $targetSlot"
                _snackbarMessage.value = "Message sent via SIM $targetSlot • $label"
            } else {
                _snackbarMessage.value = "Failed to send: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun simulateIncoming(address: String, senderName: String, body: String) {
        if (body.isBlank() || address.isBlank()) return
        viewModelScope.launch {
            val result = repository.simulateIncomingMessage(address.trim(), senderName.trim(), body.trim())
            if (result.isSuccess) {
                _snackbarMessage.value = "Received incoming SMS from $senderName"
            }
        }
    }

    fun deleteThread(address: String) {
        viewModelScope.launch {
            repository.deleteThread(address)
            if (_selectedAddress.value == address) {
                closeConversation()
            }
            _snackbarMessage.value = "Conversation deleted"
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
            _snackbarMessage.value = "Message deleted"
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
            _snackbarMessage.value = "Reset to sample SMS messages"
        }
    }

    fun syncDeviceSms() {
        viewModelScope.launch {
            val count = repository.syncDeviceSms()
            _snackbarMessage.value = if (count > 0) "Synced $count device SMS messages" else "No device SMS found or permission not granted"
        }
    }

    fun checkPermissionStatus() {
        val granted = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        _isSmsPermissionGranted.value = granted
        if (granted) {
            _showPermissionRationaleDialog.value = false
            _isPermissionPermanentlyDenied.value = false
        }
    }

    fun requestSmsPermission() {
        if (_isSmsPermissionGranted.value) {
            syncDeviceSms()
        } else {
            _permissionRequestTrigger.value = System.currentTimeMillis()
        }
    }

    fun onPermissionResult(granted: Boolean, shouldShowRationale: Boolean) {
        _isSmsPermissionGranted.value = granted
        refreshSimCards()
        if (granted) {
            _showPermissionRationaleDialog.value = false
            _isPermissionPermanentlyDenied.value = false
            syncDeviceSms()
            _snackbarMessage.value = "Permissions granted! Synced device messages & active SIMs"
        } else {
            _isPermissionPermanentlyDenied.value = !shouldShowRationale
            _showPermissionRationaleDialog.value = true
        }
    }

    fun openPermissionRationale() {
        _showPermissionRationaleDialog.value = true
    }

    fun dismissPermissionRationale() {
        _showPermissionRationaleDialog.value = false
    }

    fun checkDefaultSmsAppStatus() {
        _isDefaultSmsApp.value = com.example.util.DefaultSmsAppUtil.isDefaultSmsApp(getApplication())
    }

    fun setDefaultSmsDialogOpen(isOpen: Boolean) {
        _isDefaultSmsDialogOpen.value = isOpen
    }

    fun requestSetAsDefaultSmsApp() {
        _requestDefaultSmsAppTrigger.value = System.currentTimeMillis()
    }

    fun onDefaultSmsRoleResult() {
        checkDefaultSmsAppStatus()
        if (_isDefaultSmsApp.value) {
            _snackbarMessage.value = "SMS Query is now your default SMS app!"
        } else {
            _snackbarMessage.value = "Default SMS app unchanged"
        }
    }

    fun handleExternalSendIntent(address: String?, text: String?) {
        if (!address.isNullOrBlank()) {
            selectConversation(address)
        } else {
            setComposeDialogOpen(true)
        }
    }
}
