package ir.idontconnect.ui

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import ir.idontconnect.R
import ir.idontconnect.app.AppViewModel
import ir.idontconnect.app.UiMessage
import ir.idontconnect.data.profile.ProfileRepository
import ir.idontconnect.data.settings.SettingsRepository
import ir.idontconnect.data.settings.ThemeMode
import ir.idontconnect.engine.UnavailableXrayEngine
import ir.idontconnect.feature.home.HomeScreen
import ir.idontconnect.feature.profiles.ProfilesScreen
import ir.idontconnect.feature.settings.SettingsScreen
import ir.idontconnect.ui.theme.IdontConnectTheme
import java.io.ByteArrayOutputStream
import java.io.IOException

private enum class Destination { HOME, PROFILES, SETTINGS }

@Composable
fun IdontConnectApp(onLanguageChanged: (String) -> Unit) {
    val context = LocalContext.current
    val vm: AppViewModel = viewModel(
        factory = AppViewModel.Factory(
            ProfileRepository(context.applicationContext),
            SettingsRepository(context.applicationContext),
            UnavailableXrayEngine(),
        ),
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val qrScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { scannedValue -> vm.importProfile(scannedValue, null) }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.readTextLimited(uri, maxBytes = 2 * 1024 * 1024) }
                .onSuccess { content -> vm.importProfile(content, null) }
                .onFailure { error ->
                    vm.showMessage(
                        if (error is IllegalArgumentException) UiMessage.FILE_TOO_LARGE_OR_EMPTY
                        else UiMessage.FILE_READ_FAILED,
                    )
                }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }
    var showImportDialog by rememberSaveable { mutableStateOf(false) }
    var importText by rememberSaveable { mutableStateOf("") }
    var importName by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.settings.languageTag) {
        val tag = state.settings.languageTag
        if (tag != "system") onLanguageChanged(tag)
    }
    val messageText = when (val message = state.message) {
        null -> null
        else -> stringResource(message.toResourceId())
    }
    LaunchedEffect(messageText) {
        messageText?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessage()
        }
    }

    IdontConnectTheme(state.settings.theme, state.settings.accentIndex) {
        val effectiveLanguage = if (state.settings.languageTag == "system") {
            context.resources.configuration.locales[0]?.language.orEmpty()
        } else {
            state.settings.languageTag
        }
        val rtl = effectiveLanguage == "fa"
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides
                if (rtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr,
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = destination == Destination.HOME,
                            onClick = { destination = Destination.HOME },
                            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_home)) },
                        )
                        NavigationBarItem(
                            selected = destination == Destination.PROFILES,
                            onClick = { destination = Destination.PROFILES },
                            icon = { Icon(Icons.Rounded.FolderOpen, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_profiles)) },
                        )
                        NavigationBarItem(
                            selected = destination == Destination.SETTINGS,
                            onClick = { destination = Destination.SETTINGS },
                            icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_settings)) },
                        )
                    }
                },
            ) { padding ->
                when (destination) {
                    Destination.HOME -> HomeScreen(
                        state = state,
                        modifier = Modifier.padding(padding),
                        onConnect = vm::connect,
                        onOpenProfiles = { destination = Destination.PROFILES },
                    )
                    Destination.PROFILES -> ProfilesScreen(
                        profiles = state.profiles,
                        activeProfileId = state.activeProfileId,
                        modifier = Modifier.padding(padding),
                        onAdd = { showImportDialog = true },
                        onSelect = vm::selectProfile,
                        onRemove = vm::removeProfile,
                        onFavorite = vm::setFavorite,
                        onScanQr = {
                            qrScanner.launch(
                                ScanOptions()
                                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                    .setPrompt(context.getString(R.string.scan_qr_prompt))
                                    .setBeepEnabled(false)
                                    .setOrientationLocked(false),
                            )
                        },
                    )
                    Destination.SETTINGS -> SettingsScreen(
                        settings = state.settings,
                        modifier = Modifier.padding(padding),
                        onTheme = vm::setTheme,
                        onLanguage = { tag ->
                            vm.setLanguage(tag)
                            onLanguageChanged(if (tag == "system") "" else tag)
                        },
                        onAccent = vm::setAccent,
                        onGlow = vm::setGlow,
                        onAnimation = vm::setAnimation,
                    )
                }
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.import_profile_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = importName,
                        onValueChange = { importName = it },
                        label = { Text(stringResource(R.string.profile_name_optional)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        label = { Text(stringResource(R.string.profile_link_or_json)) },
                        minLines = 3,
                        maxLines = 6,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            importText = clipboard.primaryClip
                                ?.getItemAt(0)
                                ?.coerceToText(context)
                                ?.toString()
                                .orEmpty()
                        }) { Text(stringResource(R.string.paste_clipboard)) }
                        TextButton(
                            onClick = {
                                filePicker.launch(
                                    arrayOf("text/*", "application/json", "application/octet-stream"),
                                )
                            },
                        ) {
                            Text(stringResource(R.string.import_file))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.importProfile(importText, importName.takeIf(String::isNotBlank))
                    importText = ""
                    importName = ""
                    showImportDialog = false
                }) { Text(stringResource(R.string.import_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@androidx.compose.runtime.Composable
private fun UiMessage.toResourceId(): Int = when (this) {
    UiMessage.PROFILE_SAVED_ENGINE_PENDING -> R.string.message_profile_saved_engine_pending
    UiMessage.DUPLICATE_PROFILE -> R.string.message_duplicate_profile
    UiMessage.EMPTY_PROFILE_INPUT -> R.string.message_empty_profile
    UiMessage.UNSUPPORTED_PROFILE_SCHEME -> R.string.message_unsupported_scheme
    UiMessage.MALFORMED_PROFILE_LINK -> R.string.message_malformed_link
    UiMessage.INVALID_XRAY_JSON -> R.string.message_invalid_json
    UiMessage.ADD_PROFILE_BEFORE_CONNECTING -> R.string.message_add_profile_first
    UiMessage.ENGINE_UNAVAILABLE -> R.string.message_engine_unavailable
    UiMessage.FILE_TOO_LARGE_OR_EMPTY -> R.string.file_too_large_or_empty
    UiMessage.FILE_READ_FAILED -> R.string.file_read_failed
}


private fun Context.readTextLimited(uri: Uri, maxBytes: Int): String {
    val input = contentResolver.openInputStream(uri)
        ?: throw IOException("Could not open selected document")
    input.use { stream ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var total = 0
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            total += count
            if (total > maxBytes) throw IllegalArgumentException("Document exceeds the import size limit")
            output.write(buffer, 0, count)
        }
        return output.toString(Charsets.UTF_8.name())
    }
}
