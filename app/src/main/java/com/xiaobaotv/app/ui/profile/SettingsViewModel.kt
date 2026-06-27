package com.xiaobaotv.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaobaotv.app.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val autoNextEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.autoNextEnabled.collect { enabled ->
                _uiState.value = SettingsUiState(autoNextEnabled = enabled)
            }
        }
    }

    fun setAutoNextEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoNextEnabled(enabled)
        }
    }
}
