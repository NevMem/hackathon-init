package com.nevmem.survey.ui.settings

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nevmem.survey.BuildConfig
import com.nevmem.survey.R
import com.nevmem.survey.preferences.PreferencesService
import com.nevmem.survey.settings.api.SettingsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val DEVELOPER_SETTINGS_COUNTER = "developers-settings-counter"

class SettingsScreenViewModel(
    private val background: CoroutineScope,
    private val settingsService: SettingsService,
    private val preferencesService: PreferencesService,
) : ViewModel() {
    val uiState = mutableStateOf<List<SettingsScreenItem>>(emptyList())

    private val isDevelopersSettingsEnabled: Boolean
        get() {
            return (preferencesService.get(DEVELOPER_SETTINGS_COUNTER)?.toIntOrNull() ?: 0) >= 5
        }

    init {
        updateUiState()
    }

    fun onTitleClick() {
        var count = preferencesService.get(DEVELOPER_SETTINGS_COUNTER)?.toIntOrNull() ?: 0
        count += 1
        preferencesService.put(DEVELOPER_SETTINGS_COUNTER, count.toString())
        updateUiState()
    }

    private fun updateUiState() {
        background.launch(Dispatchers.Main) {
            val items = mutableListOf(
                HeaderSettingsScreenItem,
                BlockSettingsScreenItem(
                    listOf(
                        SwitchSettingsScreenItem(
                            R.string.notifications_enabled_setting_title,
                            settingsService.isPushNotificationsEnabled,
                        ),
                    ),
                ),
            )

            if (isDevelopersSettingsEnabled) {
                items += DeveloperSettingsHomeScreenItem
            }

            uiState.value = items + AboutSettingsScreenItem(BuildConfig.VERSION_NAME)
        }
    }
}
