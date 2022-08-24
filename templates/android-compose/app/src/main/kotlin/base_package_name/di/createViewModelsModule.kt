package com.nevmem.survey.di

import com.nevmem.survey.ui.home.HomeScreenViewModel
import com.nevmem.survey.ui.join.JoinScreenViewModel
import com.nevmem.survey.ui.settings.DevSettingsScreenViewModel
import com.nevmem.survey.ui.settings.SettingsScreenViewModel
import com.nevmem.survey.ui.splash.EthnoSplashScreenViewModel
import com.nevmem.survey.ui.survey.SurveyScreenViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun createViewModelsModule() = module {
    viewModel { JoinScreenViewModel(get(), get(), get()) }
    viewModel { EthnoSplashScreenViewModel(get(), get(BACKGROUND_SCOPE_QUALIFIER)) }
    viewModel { SurveyScreenViewModelImpl(get(), get(BACKGROUND_SCOPE_QUALIFIER), get(), get(), get()) }
    viewModel { HomeScreenViewModel(get(BACKGROUND_SCOPE_QUALIFIER), get(), get(), get()) }
    viewModel { SettingsScreenViewModel(get(BACKGROUND_SCOPE_QUALIFIER), get(), get()) }
    viewModel { DevSettingsScreenViewModel(get()) }
}
