package com.nevmem.survey.ui.splash

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nevmem.survey.service.survey.SurveyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class EthnoSplashScreenViewModel(
    private val surveyService: SurveyService,
    background: CoroutineScope,
) : ViewModel() {
    enum class Destination {
        Survey,
        Join,
    }

    val destination = mutableStateOf<Destination?>(null)

    private val job: Job = background.launch {
        val survey = surveyService.surveys.first()
        withContext(Dispatchers.Main) {
            if (survey == null) {
                destination.value = Destination.Join
            } else {
                destination.value = Destination.Survey
            }
        }
    }
}
