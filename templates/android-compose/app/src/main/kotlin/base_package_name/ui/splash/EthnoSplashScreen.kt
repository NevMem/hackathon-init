package com.nevmem.survey.ui.splash

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.koin.androidx.compose.viewModel

@Composable
fun EthnoSplashScreen(navController: NavController) {

    val viewModel: EthnoSplashScreenViewModel by viewModel()

    Text(
        text = "Ethnosurvey",
        style = MaterialTheme.typography.h3,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .wrapContentWidth()
            .wrapContentHeight()
    )
    LaunchedEffect(viewModel.destination.value) {
        navController.navigate("home")
        /* if (viewModel.destination.value == EthnoSplashScreenViewModel.Destination.Join) {
            navController.navigate("join")
        } else if (viewModel.destination.value == EthnoSplashScreenViewModel.Destination.Survey) {
            navController.navigate("survey")
        } */
    }
}
