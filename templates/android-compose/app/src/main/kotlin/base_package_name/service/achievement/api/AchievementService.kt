package com.nevmem.survey.service.achievement.api

import kotlinx.coroutines.flow.Flow

interface AchievementService {
    val achievements: Flow<List<Achievement>>

    fun reportSurveyCompleted()
}
