package com.nevmem.survey.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.koin.androidx.compose.viewModel

@Composable
fun DevSettingsScreen(
    navController: NavController,
) {
    val vm by viewModel<DevSettingsScreenViewModel>()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        vm.uiState.value.forEach {
            item {
                SettingsItemBuilder(
                    navController = navController,
                    item = it,
                )
            }
        }
    }
}
