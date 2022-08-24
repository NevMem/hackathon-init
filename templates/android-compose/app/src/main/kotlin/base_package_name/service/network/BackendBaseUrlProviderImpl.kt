package com.nevmem.survey.service.network

import com.nevmem.survey.network.api.BackendBaseUrlProvider
import com.nevmem.survey.settings.api.SettingsService

class BackendBaseUrlProviderImpl(
    private val settingsService: SettingsService,
) : BackendBaseUrlProvider {
    override suspend fun provideBaseUrl(): String {
        return if (settingsService.isHttpBackendUrlEnabled.value) {
            "http://ethnosurvey.com"
        } else {
            "https://ethnosurvey.com"
        }
    }
}
