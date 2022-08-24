package com.nevmem.survey.ui.survey

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nevmem.survey.R
import com.nevmem.survey.data.answer.QuestionAnswer

@Composable
internal fun RadioQuestionImpl(
    item: RadioQuestion,
    setCurrentAnswer: (QuestionAnswer) -> Unit,
) {
    var selectedId: String? by rememberSaveable(item) { mutableStateOf(null) }

    QuestionCard {
        Column {
            Text(
                item.title,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(8.dp)
            )
            item.variants.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = it.id == selectedId,
                        onClick = {
                            selectedId = it.id
                            setCurrentAnswer(QuestionAnswer.RadioQuestionAnswer(it.id))
                        },
                    )
                    Text(text = it.variant)
                }
            }
        }
    }
}

@Composable
fun FancyCardView(
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = 8.dp,
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}

@Preview
@Composable
fun FancyCardViewDemo() {
    FancyCardView {
        Column {
            Text(text = "header", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(text = "subtitle", style = MaterialTheme.typography.subtitle1)
            Text(text = "regular text", style = MaterialTheme.typography.body1)
        }
    }
}

@Preview
@Composable
fun SendingAnswersDemo() {
    Column {
        SendingAnswersView(item = SendingAnswers.Sending(null))
        SendingAnswersView(item = SendingAnswers.Sending(0.67f))
        SendingAnswersView(item = SendingAnswers.Error("Не удалось отправить ответы"))
        SendingAnswersView(item = SendingAnswers.Success)
    }
}

@Composable
internal fun SendingAnswersView(item: SurveyScreenItem) {
    FancyCardView {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (item) {
                is SendingAnswers.Sending -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Отправляем ответы на вопросы",
                            style = MaterialTheme.typography.h6,
                        )
                        Text(
                            "Пожалуйста не закрывайте приложение",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.subtitle2,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (item.progress != null) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    progress = item.progress,
                                )
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colors.primary.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                }
                is SendingAnswers.Error -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(id = R.string.sending_answers_failed),
                            style = MaterialTheme.typography.h6,
                        )
                        Text(
                            stringResource(id = R.string.sending_answers_failed_description),
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.subtitle2,
                        )
                        Text(
                            item.message,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                SendingAnswers.Success -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Ответы на вопросы отправлены",
                            style = MaterialTheme.typography.h6,
                        )

                        Text(
                            "Спасибо",
                            modifier = Modifier.padding(top = 12.dp),
                            style = MaterialTheme.typography.subtitle2,
                        )
                    }
                }
                else -> throw IllegalStateException()
            }
        }
    }
}

@Preview
@Composable
private fun RadioQuestionImplPreview() {
    RadioQuestionImpl(
        RadioQuestion(
            "radio?",
            variants = listOf(
                RadioQuestion.QuestionVariant("0", "first"),
                RadioQuestion.QuestionVariant("1", "second"),
                RadioQuestion.QuestionVariant("2", "third"),
                RadioQuestion.QuestionVariant("3", "forth"),
            )
        ),
        setCurrentAnswer = {},
    )
}

@Composable
internal fun QuestionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colors.primary)
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}
