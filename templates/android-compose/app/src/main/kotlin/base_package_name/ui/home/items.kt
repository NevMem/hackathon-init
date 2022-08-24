package com.nevmem.survey.ui.home

import com.nevmem.survey.data.survey.Survey
import com.nevmem.survey.service.achievement.api.Achievement

sealed class HomeScreenItem

object HomeScreenHeader : HomeScreenItem()
data class HomeScreenFooter(val canStartSurvey: Boolean) : HomeScreenItem()

sealed class SurveyState : HomeScreenItem() {
    object NoSurvey : SurveyState()
    data class AlreadyAnsweredSurvey(
        val survey: Survey,
        val canAnswerInSeconds: Long?,
    ) : SurveyState()
    data class ReadySurvey(val survey: Survey) : SurveyState()
}

data class AchievementsState(
    val achievements: List<Achievement>,
) : HomeScreenItem()
