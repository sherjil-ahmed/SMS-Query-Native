package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.dialogs.ComposeDialog
import com.example.ui.dialogs.DefaultSmsInfoDialog
import com.example.ui.dialogs.DualSimDetailsDialog
import com.example.ui.dialogs.SimulateSmsDialog
import com.example.ui.dialogs.SmsPermissionRationaleDialog
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.QueryScreen
import com.example.ui.theme.SmsQueryTheme
import com.example.util.DefaultSmsAppUtil
import com.example.viewmodel.SmsViewModel

class MainActivity : ComponentActivity() {

    private var pendingIntentAddress by mutableStateOf<String?>(null)
    private var pendingIntentText by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        extractIntentData(intent)

        setContent {
            val viewModel: SmsViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            LaunchedEffect(pendingIntentAddress, pendingIntentText) {
                if (pendingIntentAddress != null || pendingIntentText != null) {
                    viewModel.handleExternalSendIntent(pendingIntentAddress, pendingIntentText)
                    pendingIntentAddress = null
                    pendingIntentText = null
                }
            }

            SmsQueryTheme(themeMode = themeMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractIntentData(intent)
    }

    private fun extractIntentData(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_SENDTO || action == Intent.ACTION_VIEW) {
            val uri = intent.data
            val scheme = uri?.scheme
            if (scheme == "sms" || scheme == "smsto" || scheme == "mms" || scheme == "mmsto") {
                val ssp = uri.schemeSpecificPart
                val cleanAddress = ssp?.substringBefore("?") ?: ""
                if (cleanAddress.isNotBlank()) {
                    pendingIntentAddress = cleanAddress
                }
            }
            val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: intent.getStringExtra("sms_body")
            if (!text.isNullOrBlank()) {
                pendingIntentText = text
            }
        } else if (action == Intent.ACTION_SEND) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                pendingIntentText = text
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: SmsViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentScreen by viewModel.currentScreen.collectAsState()
    val isComposeOpen by viewModel.isComposeDialogOpen.collectAsState()
    val isSimulateOpen by viewModel.isSimulateDialogOpen.collectAsState()
    val isSimSettingsOpen by viewModel.isSimSettingsDialogOpen.collectAsState()
    val isDefaultSmsDialogOpen by viewModel.isDefaultSmsDialogOpen.collectAsState()
    val isDefaultSmsApp by viewModel.isDefaultSmsApp.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val showRationale by viewModel.showPermissionRationaleDialog.collectAsState()
    val isPermanentlyDenied by viewModel.isPermissionPermanentlyDenied.collectAsState()
    val requestTrigger by viewModel.permissionRequestTrigger.collectAsState()
    val requestDefaultSmsTrigger by viewModel.requestDefaultSmsAppTrigger.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Comprehensive runtime SMS & Phone permissions request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_SMS] == true
        val shouldShowRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_SMS)
        } ?: true

        viewModel.onPermissionResult(readGranted, shouldShowRationale)
    }

    // RoleManager / Default SMS App request launcher
    val defaultSmsRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onDefaultSmsRoleResult()
    }

    // Launch permission request when triggered from ViewModel
    LaunchedEffect(requestTrigger) {
        if (requestTrigger != null) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS
                )
            )
        }
    }

    // Launch default SMS app role request when triggered from ViewModel
    LaunchedEffect(requestDefaultSmsTrigger) {
        if (requestDefaultSmsTrigger != null && activity != null) {
            DefaultSmsAppUtil.requestDefaultSmsApp(activity, defaultSmsRoleLauncher)
        }
    }

    // Lifecycle observer to re-check permissions, active SIM cards, and default SMS app status on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkDefaultSmsAppStatus()
                viewModel.checkPermissionStatus()
                viewModel.refreshSimCards()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermissionStatus()
        viewModel.checkDefaultSmsAppStatus()
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    SmsViewModel.Screen.INBOX -> {
                        InboxScreen(
                            viewModel = viewModel,
                            onOpenQuery = { viewModel.navigateTo(SmsViewModel.Screen.QUERY_RESULTS) }
                        )
                    }
                    SmsViewModel.Screen.QUERY_RESULTS -> {
                        QueryScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateTo(SmsViewModel.Screen.INBOX) }
                        )
                    }
                    SmsViewModel.Screen.CONVERSATION -> {
                        ConversationScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.closeConversation() }
                        )
                    }
                }
            }
        }

        // Dialogs
        if (isComposeOpen) {
            val simCards by viewModel.availableSimCards.collectAsState()
            val currentSimSlot by viewModel.selectedSimSlot.collectAsState()
            ComposeDialog(
                availableSimCards = simCards,
                defaultSimSlot = currentSimSlot,
                onDismiss = { viewModel.setComposeDialogOpen(false) },
                onSend = { address, name, body, simSlot ->
                    viewModel.sendMessage(address, name, body, simSlot = simSlot)
                    viewModel.setComposeDialogOpen(false)
                }
            )
        }

        if (isSimulateOpen) {
            SimulateSmsDialog(
                onDismiss = { viewModel.setSimulateDialogOpen(false) },
                onSimulate = { address, name, body ->
                    viewModel.simulateIncoming(address, name, body)
                    viewModel.setSimulateDialogOpen(false)
                }
            )
        }

        if (isSimSettingsOpen) {
            val simCards by viewModel.availableSimCards.collectAsState()
            val currentSimSlot by viewModel.selectedSimSlot.collectAsState()
            DualSimDetailsDialog(
                simCards = simCards,
                selectedSimSlot = currentSimSlot,
                onSelectSimSlot = { slot -> viewModel.selectSimSlot(slot) },
                onRefresh = { viewModel.refreshSimCards() },
                onDismiss = { viewModel.setSimSettingsDialogOpen(false) }
            )
        }

        if (isDefaultSmsDialogOpen) {
            DefaultSmsInfoDialog(
                isCurrentlyDefault = isDefaultSmsApp,
                onSetAsDefault = { viewModel.requestSetAsDefaultSmsApp() },
                onOpenSystemSettings = { DefaultSmsAppUtil.openDefaultAppsSettings(context) },
                onDismiss = { viewModel.setDefaultSmsDialogOpen(false) }
            )
        }

        if (showRationale) {
            SmsPermissionRationaleDialog(
                isPermanentlyDenied = isPermanentlyDenied,
                onDismiss = { viewModel.dismissPermissionRationale() },
                onRequestPermission = { viewModel.requestSmsPermission() }
            )
        }
    }
}
