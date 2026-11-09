package com.npplayer.feature.privatevault.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.npplayer.core.data.database.entity.VaultVideoEntity
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.ui.component.NPPrimaryButton
import com.npplayer.core.ui.component.NPSecondaryButton
import com.npplayer.core.ui.theme.NPBlack
import com.npplayer.core.ui.theme.NPWhite
import com.npplayer.core.ui.theme.NPYellow
import com.npplayer.feature.privatevault.PrivateVaultViewModel
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun PrivateVaultScreen(
    onBack: () -> Unit,
    onPlayVideo: (String, String) -> Unit,
    viewModel: PrivateVaultViewModel = hiltViewModel()
) {
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val vaultVideos by viewModel.vaultVideos.collectAsState()
    val availableVideos by viewModel.availableVideos.collectAsState()
    val savedPin by viewModel.savedPin.collectAsState()
    val pinInput by viewModel.pinInput.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSettingUpPin by viewModel.isSettingUpPin.collectAsState()

    val context = LocalContext.current
    val activity = context as? FragmentActivity

    fun triggerBiometrics() {
        if (activity == null) return
        val biometricManager = BiometricManager.from(activity)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(activity)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock NP Player Vault")
                .setSubtitle("Confirm identity to access protected videos")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        viewModel.unlockWithBiometric()
                    }
                }
            )
            biometricPrompt.authenticate(promptInfo)
        }
    }

    LaunchedEffect(savedPin) {
        if (savedPin != null && !isUnlocked) {
            triggerBiometrics()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (isUnlocked) "Private Vault (${vaultVideos.size})" else "Private Vault",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )

                    if (isUnlocked) {
                        IconButton(onClick = { viewModel.lockVault() }) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Lock Vault",
                                tint = NPYellow
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isUnlocked) {
                PinLockScreen(
                    hasSavedPin = savedPin != null && !isSettingUpPin,
                    pinInput = pinInput,
                    errorMessage = errorMessage,
                    onDigit = { viewModel.onDigitEntered(it) },
                    onBackspace = { viewModel.onBackspace() },
                    onTriggerBiometrics = { triggerBiometrics() },
                    onResetPinSetup = { viewModel.resetPinSetup() }
                )
            } else {
                VaultVideosScreen(
                    vaultVideos = vaultVideos,
                    availableVideos = availableVideos,
                    onPlayVideo = onPlayVideo,
                    onImportVideo = { viewModel.moveToVault(it) },
                    onRestore = { viewModel.restoreFromVault(it) },
                    onDelete = { viewModel.deleteFromVault(it) }
                )
            }
        }
    }
}

@Composable
private fun PinLockScreen(
    hasSavedPin: Boolean,
    pinInput: String,
    errorMessage: String?,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onTriggerBiometrics: () -> Unit,
    onResetPinSetup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(color = NPYellow, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasSavedPin) Icons.Outlined.Lock else Icons.Outlined.Security,
                    contentDescription = null,
                    tint = NPBlack,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (hasSavedPin) "Enter Vault PIN" else "Set 4-Digit Vault PIN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage ?: if (hasSavedPin) "Enter your PIN or use biometrics to open" else "Create a master PIN to protect hidden videos",
                style = MaterialTheme.typography.bodyMedium,
                color = if (errorMessage != null) NPYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 4 Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) NPYellow else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (isFilled) NPYellow else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        // Numeric Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("bio", "0", "del")
            )

            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (key in row) {
                        when (key) {
                            "del" -> {
                                KeypadCircleButton(
                                    onClick = onBackspace
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            "bio" -> {
                                if (hasSavedPin) {
                                    KeypadCircleButton(
                                        onClick = onTriggerBiometrics
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Fingerprint,
                                            contentDescription = "Biometrics",
                                            tint = NPYellow,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(72.dp))
                                }
                            }
                            else -> {
                                KeypadCircleButton(
                                    onClick = { onDigit(key) }
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun KeypadCircleButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.size(72.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultVideosScreen(
    vaultVideos: List<VaultVideoEntity>,
    availableVideos: List<VideoEntity>,
    onPlayVideo: (String, String) -> Unit,
    onImportVideo: (VideoEntity) -> Unit,
    onRestore: (VaultVideoEntity) -> Unit,
    onDelete: (VaultVideoEntity) -> Unit
) {
    var showImportSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (vaultVideos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(color = MaterialTheme.colorScheme.surface, shape = CircleShape)
                        .border(2.dp, NPYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LockOpen,
                        contentDescription = null,
                        tint = NPYellow,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Vault is Empty",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Import sensitive videos to keep them hidden with hardware encryption and screenshot protection.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                NPPrimaryButton(
                    onClick = { showImportSheet = true }
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import Video to Vault",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hidden Videos (${vaultVideos.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        NPSecondaryButton(
                            onClick = { showImportSheet = true }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Add Videos")
                        }
                    }
                }

                items(vaultVideos, key = { it.id }) { vaultVideo ->
                    VaultVideoCard(
                        video = vaultVideo,
                        onClick = { onPlayVideo(vaultVideo.vaultPath, vaultVideo.displayName) },
                        onRestore = { onRestore(vaultVideo) },
                        onDelete = { onDelete(vaultVideo) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showImportSheet) {
            ImportVideosModalSheet(
                availableVideos = availableVideos,
                onDismiss = { showImportSheet = false },
                onSelectVideo = { video ->
                    onImportVideo(video)
                    showImportSheet = false
                }
            )
        }
    }
}

@Composable
private fun VaultVideoCard(
    video: VaultVideoEntity,
    onClick: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NPBlack)
            ) {
                AsyncImage(
                    model = File(video.vaultPath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Duration badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(color = NPYellow, shape = RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = formatDuration(video.durationMs),
                        color = NPBlack,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatFileSize(video.sizeBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Outlined.Restore,
                        contentDescription = "Restore to Library",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = NPYellow
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportVideosModalSheet(
    availableVideos: List<VideoEntity>,
    onDismiss: () -> Unit,
    onSelectVideo: (VideoEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Video to Protect",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (availableVideos.isEmpty()) {
                Text(
                    text = "No local videos found to import.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableVideos, key = { it.id }) { video ->
                        Surface(
                            onClick = { onSelectVideo(video) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 64.dp, height = 40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NPBlack)
                                ) {
                                    AsyncImage(
                                        model = File(video.path),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = video.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatDuration(video.durationMs)} • ${formatFileSize(video.sizeBytes)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Move to Vault",
                                    tint = NPYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

private fun formatFileSize(bytes: Long): String {
    val mb = bytes / (1024f * 1024f)
    return if (mb >= 1024) {
        String.format(Locale.getDefault(), "%.1f GB", mb / 1024f)
    } else {
        String.format(Locale.getDefault(), "%.1f MB", mb)
    }
}
