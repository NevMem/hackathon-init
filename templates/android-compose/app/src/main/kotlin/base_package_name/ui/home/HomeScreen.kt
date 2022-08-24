package com.nevmem.survey.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nevmem.survey.R
import com.nevmem.survey.service.achievement.api.Achievement
import com.nevmem.survey.ui.survey.FancyCardView
import org.koin.androidx.compose.viewModel

@Composable
fun HomeScreen(
    navController: NavController,
) {
    val vm by viewModel<HomeScreenViewModel>()
    val items = vm.uiState.value

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = remember {
            object : Arrangement.Vertical {
                override fun Density.arrange(
                    totalSize: Int,
                    sizes: IntArray,
                    outPositions: IntArray
                ) {
                    var currentOffset = 0
                    sizes.forEachIndexed { index, size ->
                        if (index == sizes.lastIndex) {
                            outPositions[index] = totalSize - size
                        } else {
                            outPositions[index] = currentOffset
                            currentOffset += size
                        }
                    }
                }
            }
        },
    ) {
        items.forEach { homeItem ->
            when (homeItem) {
                is AchievementsState -> item { AchievementsView(homeItem.achievements) }
                is SurveyState -> item { SurveyView(navController, homeItem) { vm.leaveSurvey() } }
                HomeScreenHeader -> item { HeaderItem() }
                is HomeScreenFooter -> item { FooterView(navController, homeItem) }
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = remember {
            object : Arrangement.Vertical {
                override fun Density.arrange(
                    totalSize: Int,
                    sizes: IntArray,
                    outPositions: IntArray
                ) {
                    var currentOffset = 0
                    sizes.forEachIndexed { index, size ->
                        if (index == sizes.lastIndex) {
                            outPositions[index] = totalSize - size
                        } else {
                            outPositions[index] = currentOffset
                            currentOffset += size
                        }
                    }
                }
            }
        },
    ) {
        item { HeaderItem() }
        item { AchievementsView(emptyList()) }
        item { SurveyView(rememberNavController(), SurveyState.NoSurvey) { } }
        item { FooterView(rememberNavController(), HomeScreenFooter(true)) }
    }
}

@Composable
private fun FooterView(
    navController: NavController,
    item: HomeScreenFooter,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { navController.navigate("settings") }) {
            Icon(imageVector = Icons.Filled.Settings, contentDescription = "settings button")
        }
        Button(
            onClick = { navController.navigate("survey") },
            enabled = item.canStartSurvey,
        ) {
            Text(
                stringResource(id = R.string.take_survey_long),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
            )
        }
    }
}

@Composable
private fun HeaderItem() {
    Text(
        stringResource(id = R.string.app_name),
        modifier = Modifier
            .padding(top = 32.dp, bottom = 16.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h3,
    )
}

@Composable
private fun AchievementsView(achievements: List<Achievement>) {
    FancyCardView {
        Column {
            Text(
                stringResource(id = R.string.achievements_title),
                style = MaterialTheme.typography.subtitle1,
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState()),
            ) {
                achievements.forEach {
                    AchievementView(it)
                }
            }
        }
    }
}

@Composable
private fun SurveyView(
    navController: NavController,
    item: SurveyState,
    leaveSurvey: () -> Unit,
) {
    if (item is SurveyState.NoSurvey) {
        NoSurveyView(navController = navController)
    } else {
        FancyCardView {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                when (item) {
                    is SurveyState.AlreadyAnsweredSurvey -> {
                        AlreadyAnsweredSurveyView(item, leaveSurvey)
                    }
                    is SurveyState.ReadySurvey -> {
                        ReadySurveyView(
                            navController = navController,
                            item = item,
                            leaveSurvey = leaveSurvey,
                        )
                    }
                    else -> throw IllegalStateException()
                }
            }
        }
    }
}

@Composable
private fun NoSurveyView(
    navController: NavController,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = { navController.navigate("join") }) {
            Text(stringResource(id = R.string.join_survey))
        }
    }
}

@Composable
private fun AlreadyAnsweredSurveyView(
    item: SurveyState.AlreadyAnsweredSurvey,
    leaveSurvey: () -> Unit,
) {
    var dialogOpened by remember { mutableStateOf(false) }

    val canAnswerInSeconds = item.canAnswerInSeconds
    if (canAnswerInSeconds == null) {
        Text(
            item.survey.name,
            style = MaterialTheme.typography.subtitle1,
        )
        Text(
            stringResource(id = R.string.already_answered_survey_max_times),
            style = MaterialTheme.typography.body2,
        )
        OutlinedButton(
            onClick = { dialogOpened = true }
        ) {
            Text(stringResource(id = R.string.leave_survey))
        }
    } else {
        Text(
            item.survey.name,
            style = MaterialTheme.typography.subtitle1,
        )
        Text(
            stringResource(id = R.string.can_answer_survey_in, canAnswerInSeconds / 60),
            style = MaterialTheme.typography.subtitle2,
        )
        OutlinedButton(
            onClick = { dialogOpened = true }
        ) {
            Text(stringResource(id = R.string.leave_survey))
        }
    }

    if (dialogOpened) {
        AlertDialog(
            title = { Text(stringResource(id = R.string.confirm_leave_survey)) },
            onDismissRequest = { dialogOpened = false },
            confirmButton = { Button(onClick = { leaveSurvey(); dialogOpened = false }) { Text(stringResource(id = R.string.ok)) } },
            dismissButton = { OutlinedButton(onClick = { dialogOpened = false }) { Text(stringResource(id = R.string.cancel)) } },
        )
    }
}

@Composable
private fun ReadySurveyView(
    navController: NavController,
    item: SurveyState.ReadySurvey,
    leaveSurvey: () -> Unit,
) {
    var dialogOpened by remember { mutableStateOf(false) }

    Text(
        stringResource(id = R.string.home_screen_joined_survey),
        style = MaterialTheme.typography.subtitle1,
    )
    Text(
        item.survey.name,
        modifier = Modifier.padding(top = 8.dp),
        style = MaterialTheme.typography.body2,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Button(
            onClick = { navController.navigate("survey") },
        ) {
            Text(stringResource(id = R.string.take_survey))
        }
        OutlinedButton(
            onClick = { dialogOpened = true }
        ) {
            Text(stringResource(id = R.string.leave_survey))
        }
    }

    if (dialogOpened) {
        AlertDialog(
            title = { Text(stringResource(id = R.string.confirm_leave_survey)) },
            onDismissRequest = { dialogOpened = false },
            confirmButton = { Button(onClick = { leaveSurvey(); dialogOpened = false }) { Text(stringResource(id = R.string.ok)) } },
            dismissButton = { OutlinedButton(onClick = { dialogOpened = false }) { Text(stringResource(id = R.string.cancel)) } },
        )
    }
}
