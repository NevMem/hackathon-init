package com.nevmem.survey.service.commonquestions

import com.nevmem.survey.data.answer.QuestionAnswer
import com.nevmem.survey.data.question.CommonQuestion
import com.nevmem.survey.preferences.PreferencesService
import com.nevmem.survey.report.report
import kotlinx.serialization.json.Json

class CommonQuestionsService(
    preferencesService: PreferencesService,
) {
    private val delegate = preferencesService.createDelegate("common-questions")

    fun getAnswerForCommonQuestion(commonQuestion: CommonQuestion): QuestionAnswer? {
        return delegate.get(commonQuestion.id)?.let {
            try {
                Json.decodeFromString(QuestionAnswer.serializer(), it)
            } catch (exception: Exception) {
                report("common-question-decoding", exception)
                null
            }
        }
    }

    fun saveAnswerForCommonQuestion(commonQuestion: CommonQuestion, answer: QuestionAnswer) {
        try {
            delegate.put(commonQuestion.id, Json.encodeToString(QuestionAnswer.serializer(), answer))
        } catch (exception: Exception) {
            report("common-question-encoding", exception)
        }
    }
}
