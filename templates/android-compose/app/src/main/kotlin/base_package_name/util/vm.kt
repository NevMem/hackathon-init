package com.nevmem.survey.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent

fun ViewModel.injectBackgroundScope() = KoinJavaComponent.inject<CoroutineScope>(
    CoroutineScope::class.java,
    qualifier = named("background"),
)
fun ViewModel.injectUiScope() = KoinJavaComponent.inject<CoroutineScope>(
    CoroutineScope::class.java,
    qualifier = named("ui"),
)
