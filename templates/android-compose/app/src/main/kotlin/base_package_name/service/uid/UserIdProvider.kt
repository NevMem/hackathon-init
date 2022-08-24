package com.nevmem.survey.service.uid

import com.nevmem.survey.data.user.UserId
import com.nevmem.survey.preferences.PreferencesService
import com.nevmem.survey.report.report
import com.nevmem.survey.settings.api.SettingsService
import java.util.UUID

private const val USER_ID_PREFS_KEY = "user_id"

class UserIdProvider(
    private val settingsService: SettingsService,
    private val preferencesService: PreferencesService,
) {

    init {
        report("user-id-provider", "init")
    }

    fun provide(): UserId {
        if (settingsService.enableNotUniqueUserIds.value) {
            return UserId(UUID.randomUUID().toString())
        }

        val userIdValue = preferencesService.get(USER_ID_PREFS_KEY)
        if (userIdValue != null) {
            return UserId(userIdValue)
        }
        val uid = UUID.randomUUID().toString()
        preferencesService.put(USER_ID_PREFS_KEY, uid)
        return UserId(uid)
    }
}
