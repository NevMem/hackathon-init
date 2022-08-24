package com.nevmem.survey.service.achievement.api

import com.nevmem.survey.preferences.PreferencesService
import com.nevmem.survey.service.achievement.internal.AchievementServiceImpl
import kotlinx.coroutines.CoroutineScope

fun createAchievementService(
    background: CoroutineScope,
    preferencesService: PreferencesService,
): AchievementService = AchievementServiceImpl(background, preferencesService)
