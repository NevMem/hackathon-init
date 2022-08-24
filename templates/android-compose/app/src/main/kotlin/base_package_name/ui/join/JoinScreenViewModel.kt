package com.nevmem.survey.ui.join

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nevmem.survey.network.api.NetworkService
import com.nevmem.survey.service.survey.SurveyService
import com.nevmem.survey.service.uid.UserIdProvider
import com.nevmem.survey.util.injectBackgroundScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JoinScreenViewModel(
    private val networkService: NetworkService,
    private val surveyService: SurveyService,
    private val userIdProvider: UserIdProvider,
) : ViewModel() {

    private val background by injectBackgroundScope()

    sealed class UiState {
        object None : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    internal val state = mutableStateOf<UiState>(UiState.None)

    private var job: Job? = null

    internal fun tryFetchSurvey(surveyId: String) {
        job?.cancel()
        job = background.launch {
            withContext(Dispatchers.Main) {
                state.value = UiState.Loading
            }

            try {
                val survey = networkService.loadSurvey(
                    userIdProvider.provide(),
                    surveyId,
                )
                withContext(Dispatchers.Main) {
                    state.value = UiState.Success
                    surveyService.saveSurvey(survey)
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    println("cur_deb $exception")
                    state.value = UiState.Error("Some error while loading survey $surveyId ${exception.message}")
                }
            }
        }
    }
}
