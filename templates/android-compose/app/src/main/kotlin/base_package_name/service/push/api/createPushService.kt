package com.nevmem.survey.service.push.api

import com.nevmem.survey.network.api.NetworkService
import com.nevmem.survey.service.push.internal.PushServiceImpl
import com.nevmem.survey.service.uid.UserIdProvider
import com.nevmem.survey.settings.api.SettingsService
import kotlinx.coroutines.CoroutineScope

fun createPushService(
    background: CoroutineScope,
    settingsService: SettingsService,
    networkService: NetworkService,
    userIdProvider: UserIdProvider,
): PushService = PushServiceImpl(
    background,
    settingsService,
    networkService,
    userIdProvider,
)
