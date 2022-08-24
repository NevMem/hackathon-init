package com.nevmem.survey.ui.survey

sealed class SurveyScreenItem

data class TextQuestion(val title: String, val maxLength: Int) : SurveyScreenItem()
data class RatingQuestion(val title: String, val min: Int, val max: Int) : SurveyScreenItem()
data class StarsQuestion(val title: String, val stars: Int) : SurveyScreenItem()
data class RadioQuestion(val title: String, val variants: List<QuestionVariant>) : SurveyScreenItem() {
    data class QuestionVariant(val id: String, val variant: String)
}

sealed class SendingAnswers : SurveyScreenItem() {
    data class Sending(
        val progress: Float?,
        val message: String = "",
    ) : SendingAnswers()
    data class Error(val message: String) : SendingAnswers()
    object Success : SendingAnswers()
}
