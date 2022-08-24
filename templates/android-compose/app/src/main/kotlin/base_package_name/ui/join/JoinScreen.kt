package com.nevmem.survey.ui.join

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nevmem.survey.R
import com.nevmem.survey.ui.survey.FancyCardView
import org.koin.androidx.compose.viewModel

@Composable
fun JoinScreen(
    navController: NavController,
    scaffoldState: ScaffoldState,
) {
    val viewModel: JoinScreenViewModel by viewModel()
    var surveyId: String by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FancyCardView {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ErrorCard(scaffoldState, viewModel.state.value)
                Text(
                    stringResource(id = R.string.enter_survey_id),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.body1,
                )

                Row(modifier = Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = surveyId,
                        onValueChange = { surveyId = it },
                        label = { Text(stringResource(R.string.survey_id_label)) }
                    )
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.tryFetchSurvey(surveyId)
                        },
                        enabled = viewModel.state.value != JoinScreenViewModel.UiState.Loading,
                    ) {
                        Text(text = "Join survey")
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = viewModel.state.value) {
        if (viewModel.state.value == JoinScreenViewModel.UiState.Success) {
            navController.navigate("home")
        }
    }
}

@Composable
private fun ErrorCard(scaffoldState: ScaffoldState, state: JoinScreenViewModel.UiState) {
    if (state is JoinScreenViewModel.UiState.Error) {
        LaunchedEffect(state.message) {
            scaffoldState.snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
        }
    }
}
