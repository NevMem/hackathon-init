package com.nevmem.survey.ui.survey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nevmem.survey.R
import com.nevmem.survey.data.answer.QuestionAnswer
import kotlinx.coroutines.launch
import org.koin.androidx.compose.viewModel

@ExperimentalComposeUiApi
@Composable
fun SurveyScreen(
    navController: NavController,
    scaffoldState: ScaffoldState,
) {
    val viewModel: SurveyScreenViewModelImpl by viewModel()
    val survey = viewModel.survey.value

    var currentAnswer: QuestionAnswer? by remember { mutableStateOf(null) }
    val setCurrentAnswer = { answer: QuestionAnswer ->
        currentAnswer = answer
    }

    val scope = rememberCoroutineScope()

    val needAnswerMessage = stringResource(id = R.string.wait_for_answer)
    val moveNext: () -> Unit = {
        if (currentAnswer == null) {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(needAnswerMessage)
            }
        } else {
            viewModel.dispatch(SurveyScreenAction.Next(currentAnswer!!))
            currentAnswer = null
        }
    }
    val send: () -> Unit = {
        if (currentAnswer == null) {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(needAnswerMessage)
            }
        } else {
            viewModel.dispatch(SurveyScreenAction.Send(currentAnswer!!))
            currentAnswer = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(survey.name, style = MaterialTheme.typography.h5)
            }

            SurveyScreenProgressBar(viewModel.uiState.value)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            SurveyScreenItemImpl(
                item = viewModel.uiState.value.currentItem,
                setCurrentAnswer = setCurrentAnswer,
            )
        }

        ActionsRowWrapper(
            navController = navController,
            actions = viewModel.uiState.value.actions,
            moveNext = moveNext,
            movePrev = { viewModel.dispatch(SurveyScreenAction.Previous) },
            send = send,
            retry = { viewModel.dispatch(SurveyScreenAction.Retry) }
        )
    }
}

@Composable
private fun TakePictureAction(navController: NavController) {
    IconButton(onClick = { navController.navigate("camera") }) {
        Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = "camera")
    }
}

@Composable
private fun ActionsRowWrapper(
    navController: NavController,
    actions: List<SurveyScreenActionType>,
    moveNext: () -> Unit,
    movePrev: () -> Unit,
    send: () -> Unit,
    retry: () -> Unit,
) {
    Box(modifier = Modifier.padding(top = 28.dp, bottom = 28.dp)) {
        ActionsRow(navController, actions, moveNext, movePrev, send, retry)
    }
}

@Composable
private fun ActionsRow(
    navController: NavController,
    actions: List<SurveyScreenActionType>,
    moveNext: () -> Unit,
    movePrev: () -> Unit,
    send: () -> Unit,
    retry: () -> Unit,
) {
    if (actions.size >= 3) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ActionsRow(
                navController = navController,
                actions = actions.subList(1, actions.size - 1),
                moveNext = moveNext,
                movePrev = movePrev,
                send = send,
                retry = retry,
            )
            ActionsRow(
                navController = navController,
                actions = listOf(actions.first(), actions.last()),
                moveNext = moveNext,
                movePrev = movePrev,
                send = send,
                retry = retry,
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = if (actions.size >= 2) Arrangement.SpaceBetween else Arrangement.SpaceAround,
        ) {
            actions.forEach {
                when (it) {
                    SurveyScreenActionType.Next -> NextAction(moveNext = moveNext)
                    SurveyScreenActionType.Send -> SendAction(send = send)
                    SurveyScreenActionType.Previous -> PrevAction(movePrev = movePrev)
                    SurveyScreenActionType.Retry -> RetryAction(retry = retry)
                    SurveyScreenActionType.TakePicture -> TakePictureAction(navController = navController)
                }
            }
        }
    }
}

@Composable
private fun NextAction(moveNext: () -> Unit) {
    Button(onClick = { moveNext() }) {
        Text(stringResource(id = R.string.next), modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp))
    }
}

@Composable
private fun PrevAction(movePrev: () -> Unit) {
    OutlinedButton(onClick = { movePrev() }) {
        Text(stringResource(id = R.string.previous), modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp))
    }
}

@Composable
private fun SendAction(send: () -> Unit) {
    Button(onClick = { send() }) {
        Text(stringResource(id = R.string.send), modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp))
    }
}

@Composable
private fun RetryAction(retry: () -> Unit) {
    Button(onClick = { retry() }) {
        Text(stringResource(id = R.string.retry), modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp))
    }
}

@Composable
private fun SurveyScreenProgressBar(uiState: SurveyScreenUiState) {
    if (uiState.progress is ProgressState.ActualProgress) {
        Text("${uiState.progress.progress} / ${uiState.progress.outOf}")

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = 8.dp),
            progress = uiState.progress.percentage,
        )
    }
}

@ExperimentalComposeUiApi
@Composable
private fun SurveyScreenItemImpl(
    item: SurveyScreenItem,
    setCurrentAnswer: (QuestionAnswer) -> Unit,
) {
    when (item) {
        is RatingQuestion -> { RatingQuestionImpl(item = item, setCurrentAnswer = setCurrentAnswer) }
        is TextQuestion -> { TextQuestionImpl(item = item, setCurrentAnswer = setCurrentAnswer) }
        is StarsQuestion -> { StarsQuestionImpl(item = item, setCurrentAnswer = setCurrentAnswer) }
        is RadioQuestion -> { RadioQuestionImpl(item = item, setCurrentAnswer = setCurrentAnswer) }
        else -> { SendingAnswersView(item = item) }
    }
}

@Composable
private fun RatingQuestionImpl(
    item: RatingQuestion,
    setCurrentAnswer: (QuestionAnswer) -> Unit,
) {
    var sliderValue by rememberSaveable(item) { mutableStateOf(item.min.toFloat()) }
    QuestionCard {
        Column {
            Text(
                item.title,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item.min.toString(),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(8.dp),
                )
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        setCurrentAnswer(QuestionAnswer.RatingQuestionAnswer(it.toInt()))
                    },
                    steps = item.max - item.min - 1,
                    valueRange = item.min.toFloat()..item.max.toFloat(),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    item.max.toString(),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun StarsQuestionImpl(
    item: StarsQuestion,
    setCurrentAnswer: (QuestionAnswer) -> Unit,
) {
    var sliderValue by rememberSaveable(item) { mutableStateOf((item.stars / 2 + 1).toFloat()) }
    QuestionCard {
        Column {
            Text(
                item.title,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "1",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(8.dp),
                )
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        setCurrentAnswer(QuestionAnswer.StarsQuestionAnswer(it.toInt()))
                    },
                    steps = item.stars - 2,
                    valueRange = 1f..item.stars.toFloat(),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    item.stars.toString(),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun TextQuestionImpl(
    item: TextQuestion,
    setCurrentAnswer: (QuestionAnswer) -> Unit,
) {
    var text by rememberSaveable(item) { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val itemsPadding = 16.dp

    QuestionCard {
        Column(modifier = Modifier) {
            Text(
                item.title,
                style = MaterialTheme.typography.h5,
                overflow = TextOverflow.Visible,
            )
            OutlinedTextField(
                modifier = Modifier
                    .padding(top = itemsPadding)
                    .fillMaxWidth(),
                value = text,
                onValueChange = {
                    if (it.length <= item.maxLength) {
                        text = it
                        setCurrentAnswer(QuestionAnswer.TextQuestionAnswer(text))
                    }
                },
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide(); focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            )
            Text(
                "${text.length} / ${item.maxLength}",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
    }
}
