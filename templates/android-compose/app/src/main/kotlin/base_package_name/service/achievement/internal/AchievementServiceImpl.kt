package com.nevmem.survey.service.achievement.internal

import com.nevmem.survey.preferences.PreferencesService
import com.nevmem.survey.report.report
import com.nevmem.survey.service.achievement.api.Achievement
import com.nevmem.survey.service.achievement.api.AchievementService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

private const val SURVEY_COMPLETED = "survey-completed"

private enum class AchievementType {
    VeryFirst,
    Counter,
}

private sealed class AchievementRule(
    val id: String,
    val report: String,
    val title: String,
) {
    class VeryFirstAchievementRule(
        id: String,
        report: String,
        title: String,
    ) : AchievementRule(id, report, title)

    class CounterAchievementRule(
        id: String,
        report: String,
        title: String,
        val steps: List<Int>,
    ) : AchievementRule(id, report, title)
}

private val AchievementRule.prefKey: String
    get() = "achievement.$report.$id"

private interface AchievementState {
    val achievement: Flow<Achievement>
}

private class VeryFirstAchievementState(
    background: CoroutineScope,
    private val preferencesService: PreferencesService,
    private val rule: AchievementRule,
    reports: Flow<String>,
) : AchievementState {
    private val mutableAchievement = MutableStateFlow<Achievement?>(null)
    override val achievement = mutableAchievement.filterNotNull()

    init {
        check(rule is AchievementRule.VeryFirstAchievementRule) { }
        background.launch {
            preferencesService.get(rule.prefKey)?.let {
                if (it == "true") {
                    mutableAchievement.emit(
                        Achievement.VeryFirstAchievement(
                            id = rule.id,
                            title = rule.title,
                        )
                    )
                }
            }

            reports.filter { it == rule.report }.collect {
                preferencesService.put(rule.prefKey, "true")
                mutableAchievement.emit(
                    Achievement.VeryFirstAchievement(
                        id = rule.id,
                        title = rule.title,
                    )
                )
            }
        }
    }
}

private class CounterAchievementState(
    background: CoroutineScope,
    private val preferencesService: PreferencesService,
    private val rule: AchievementRule,
    reports: Flow<String>,
) : AchievementState {
    private val mutableAchievement = MutableStateFlow<Achievement?>(null)
    override val achievement = mutableAchievement.filterNotNull()

    init {
        check(rule is AchievementRule.CounterAchievementRule) { }
        background.launch {
            preferencesService.get(rule.prefKey)?.toIntOrNull()?.let {
                mutableAchievement.emit(rule.buildAchievement(it))
            }

            reports.filter { it == rule.report }.collect {
                val value = (preferencesService.get(rule.prefKey)?.toIntOrNull() ?: 0) + 1
                mutableAchievement.emit(rule.buildAchievement(value))
                preferencesService.put(rule.prefKey, value.toString())
            }
        }
    }

    private fun AchievementRule.CounterAchievementRule.buildAchievement(count: Int): Achievement? {
        return steps.filter { it <= count }.maxOrNull()?.let {
            Achievement.CounterAchievement(
                id = id,
                title = title,
                result = it,
            )
        }
    }
}

private class AchievementsState(
    background: CoroutineScope,
    preferencesService: PreferencesService,
    reports: Flow<String>,
) {
    private val shared = reports.shareIn(background, SharingStarted.Eagerly)

    private val achievementStates = listOf(
        VeryFirstAchievementState(
            background,
            preferencesService,
            AchievementRule.VeryFirstAchievementRule(
                id = "very-first-survey",
                report = SURVEY_COMPLETED,
                title = "Первый пройденный опрос",
            ),
            shared,
        ),
        CounterAchievementState(
            background,
            preferencesService,
            AchievementRule.CounterAchievementRule(
                id = "surveys-counter",
                report = SURVEY_COMPLETED,
                title = "Пройдено опросов",
                steps = listOf(1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 100),
            ),
            shared,
        )
    )

    val achievements = combine(
        achievementStates.map {
            it.achievement.map<Achievement, Achievement?> { it }
                .onStart { emit(null) }
        }
    ) { it.toList().filterNotNull() }
}

internal class AchievementServiceImpl(
    private val background: CoroutineScope,
    preferencesService: PreferencesService,
) : AchievementService {
    private val reports = MutableSharedFlow<String>()
    private val achievementsState = AchievementsState(background, preferencesService, reports)
    override val achievements = achievementsState.achievements

    init {
        report("achievement-service", "init")
    }

    override fun reportSurveyCompleted() {
        background.launch {
            reports.emit(SURVEY_COMPLETED)
        }
    }
}
