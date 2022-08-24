package com.nevmem.survey.ui.settings

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nevmem.survey.R
import com.nevmem.survey.settings.api.SettingsService

class DevSettingsScreenViewModel(
    settingsService: SettingsService,
) : ViewModel() {
    val uiState = mutableStateOf(
        listOf(
            HeaderSettingsScreenItem,
            BlockSettingsScreenItem(
                SwitchSettingsScreenItem(
                    R.string.http_url_setting_title,
                    settingsService.isHttpBackendUrlEnabled,
                ),
                SwitchSettingsScreenItem(
                    R.string.enable_not_unique_user_ids,
                    settingsService.enableNotUniqueUserIds,
                ),
            )
        )
    )
}
