package com.nevmem.survey.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nevmem.survey.R
import com.nevmem.survey.service.achievement.api.Achievement

@Composable
fun AchievementView(achievement: Achievement) {
    when (achievement) {
        is Achievement.CounterAchievement -> {
            if (achievement.id == "surveys-counter") {
                SurveysCounterAchievement(achievement)
            } else {
                Text("Unsupported achievement ${achievement.title}")
            }
        }
        is Achievement.VeryFirstAchievement -> {
            if (achievement.id == "very-first-survey") {
                VeryFirstAchievement(achievement)
            } else {
                Text("Unsupported achievement ${achievement.title}")
            }
        }
    }
}

@Composable
fun SurveysCounterAchievement(achievement: Achievement.CounterAchievement) {
    var dialogOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable { dialogOpen = true }
            .background(MaterialTheme.colors.primary.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = achievement.result.toString(),
            style = MaterialTheme.typography.h5.copy(color = MaterialTheme.colors.background),
        )
    }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text(achievement.title) },
            confirmButton = {
                Button(onClick = { dialogOpen = false }, modifier = Modifier.padding(8.dp)) {
                    Text(stringResource(id = R.string.ok))
                }
            },
        )
    }
}

@Composable
fun VeryFirstAchievement(achievement: Achievement.VeryFirstAchievement) {
    var dialogOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable { dialogOpen = true }
            .background(MaterialTheme.colors.primary.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "1",
            style = MaterialTheme.typography.h5.copy(color = MaterialTheme.colors.background),
        )
        Text(
            text = "st",
            style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.background),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 6.dp, end = 6.dp),
        )
    }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text(achievement.title) },
            confirmButton = {
                Button(onClick = { dialogOpen = false }, modifier = Modifier.padding(8.dp)) {
                    Text(stringResource(id = R.string.ok))
                }
            },
        )
    }
}
