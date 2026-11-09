package com.npplayer.feature.privatevault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.npplayer.core.data.database.dao.VideoDao
import com.npplayer.core.data.database.entity.VaultVideoEntity
import com.npplayer.core.data.database.entity.VideoEntity
import com.npplayer.core.data.preferences.SettingsRepository
import com.npplayer.core.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivateVaultViewModel @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val videoDao: VideoDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val vaultVideos = vaultRepository.vaultVideos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val availableVideos = videoDao.getAllVideos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedPin = settingsRepository.vaultPinFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSettingUpPin = MutableStateFlow(false)
    val isSettingUpPin: StateFlow<Boolean> = _isSettingUpPin.asStateFlow()

    private var initialSetupPin: String? = null

    fun onDigitEntered(digit: String) {
        if (_pinInput.value.length < 4) {
            val next = _pinInput.value + digit
            _pinInput.value = next
            _errorMessage.value = null

            if (next.length == 4) {
                handlePinComplete(next)
            }
        }
    }

    fun onBackspace() {
        if (_pinInput.value.isNotEmpty()) {
            _pinInput.value = _pinInput.value.dropLast(1)
            _errorMessage.value = null
        }
    }

    private fun handlePinComplete(pin: String) {
        val currentSaved = savedPin.value
        if (currentSaved == null || _isSettingUpPin.value) {
            // First time setup
            if (initialSetupPin == null) {
                initialSetupPin = pin
                _pinInput.value = ""
                _errorMessage.value = "Confirm your 4-digit PIN"
            } else {
                if (pin == initialSetupPin) {
                    viewModelScope.launch {
                        settingsRepository.setVaultPin(pin)
                        _isSettingUpPin.value = false
                        _isUnlocked.value = true
                        _pinInput.value = ""
                        _errorMessage.value = null
                    }
                } else {
                    initialSetupPin = null
                    _pinInput.value = ""
                    _errorMessage.value = "PINs do not match. Try again."
                }
            }
        } else {
            // Validate existing PIN
            if (pin == currentSaved) {
                _isUnlocked.value = true
                _pinInput.value = ""
                _errorMessage.value = null
            } else {
                _pinInput.value = ""
                _errorMessage.value = "Incorrect PIN. Try again."
            }
        }
    }

    fun unlockWithBiometric() {
        _isUnlocked.value = true
        _errorMessage.value = null
    }

    fun lockVault() {
        _isUnlocked.value = false
        _pinInput.value = ""
        _errorMessage.value = null
    }

    fun moveToVault(video: VideoEntity) {
        viewModelScope.launch {
            vaultRepository.moveToVault(video)
        }
    }

    fun restoreFromVault(vaultVideo: VaultVideoEntity) {
        viewModelScope.launch {
            vaultRepository.restoreFromVault(vaultVideo)
        }
    }

    fun deleteFromVault(vaultVideo: VaultVideoEntity) {
        viewModelScope.launch {
            vaultRepository.deleteFromVault(vaultVideo)
        }
    }

    fun resetPinSetup() {
        _isSettingUpPin.value = true
        initialSetupPin = null
        _pinInput.value = ""
        _errorMessage.value = null
    }
}
