package com.nevmem.survey.service.survey

import android.net.Uri
import com.nevmem.survey.data.answer.QuestionAnswer
import com.nevmem.survey.data.survey.Survey
import com.nevmem.survey.network.api.NetworkService
import com.nevmem.survey.preferences.PreferencesService
import com.nevmem.survey.report.report
import com.nevmem.survey.service.uid.UserIdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class SurveyService(
    private val preferencesService: PreferencesService,
    private val networkService: NetworkService,
    private val userIdProvider: UserIdProvider,
) {
    private var currentSurvey: Survey? = null
        set(value) {
            field = value
            while (!surveyFlow.tryEmit(field)) {}
        }

    private val surveyFlow = MutableSharedFlow<Survey?>(replay = 1)
    val surveys: Flow<Survey?> = surveyFlow
    val survey: Survey
        get() = currentSurvey!!

    init {
        report("survey-service", "init")
        val prefValue = preferencesService.get("currentSurvey")
        currentSurvey = if (prefValue != null) {
            try {
                Json.decodeFromString(Survey.serializer(), prefValue)
            } catch (exception: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun leaveSurvey() {
        currentSurvey = null
        preferencesService.delete("currentSurvey")
    }

    fun saveSurvey(survey: Survey) {
        currentSurvey = survey
        preferencesService.put("currentSurvey", Json.encodeToString(Survey.serializer(), survey))
    }

    fun sendAnswer(answers: List<QuestionAnswer>, medias: List<Uri>): Flow<SendAnswerStatus> = flow {
        val steps: Float = medias.size + 2f

        val savedMedias = medias.mapIndexed { index, media ->
            val savedMedia = networkService.sendMedia(media)
            emit(SendAnswerStatus((index + 1) / steps))
            savedMedia
        }

        val gallery = savedMedias.takeIf { it.isNotEmpty() }?.let { networkService.createGallery(it) }
        emit(SendAnswerStatus((medias.size) + 1 / steps))

        networkService.sendSurvey(
            survey.surveyId,
            userIdProvider.provide(),
            answers,
            gallery,
        )

        preferencesService.put("answered-survey-${survey.id}", System.currentTimeMillis().toString())
    }
}
