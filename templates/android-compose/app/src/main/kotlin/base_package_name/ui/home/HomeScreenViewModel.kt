package com.nevmem.survey.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nevmem.survey.data.survey.Survey
import com.nevmem.survey.preferences.PreferencesService
import com.nevmem.survey.service.achievement.api.Achievement
import com.nevmem.survey.service.achievement.api.AchievementService
import com.nevmem.survey.service.survey.SurveyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeScreenViewModel(
    background: CoroutineScope,
    private val achievementService: AchievementService,
    private val surveyService: SurveyService,
    private val preferencesService: PreferencesService,
) : ViewModel() {
    val uiState = mutableStateOf<List<HomeScreenItem>>(emptyList())

    private var job: Job? = null

    init {
        job = background.launch {
            combine(
                surveyService.surveys,
                achievementService.achievements,
            ) { survey: Survey?, achievements: List<Achievement> ->
                val surveyItem = survey.item()
                listOf(
                    HomeScreenHeader,
                    achievements.item(),
                    surveyItem,
                    HomeScreenFooter(surveyItem is SurveyState.ReadySurvey),
                )
            }.collect {
                withContext(Dispatchers.Main) {
                    uiState.value = it
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    fun leaveSurvey() {
        surveyService.leaveSurvey()
    }

    private fun Survey?.item(): HomeScreenItem {
        if (this == null) {
            return SurveyState.NoSurvey
        }
        val answeredCurrentSurveyAt =
            preferencesService.get("answered-survey-$id")?.toLongOrNull() ?: 0
        if (answeredCurrentSurveyAt + surveyCoolDown >= System.currentTimeMillis() || (answeredCurrentSurveyAt != 0L && surveyCoolDown == Survey.SURVEY_COOL_DOWN_ONLY_ONCE)) {
            return SurveyState.AlreadyAnsweredSurvey(
                this,
                canAnswerInSeconds = if (surveyCoolDown == Survey.SURVEY_COOL_DOWN_ONLY_ONCE)
                    null
                else
                    (surveyCoolDown - (System.currentTimeMillis() - answeredCurrentSurveyAt)) / 1000
            )
        }
        return SurveyState.ReadySurvey(this)
    }

    private fun List<Achievement>.item(): HomeScreenItem {
        return AchievementsState(this)
    }
}
