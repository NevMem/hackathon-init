package com.nevmem.survey.service.achievement.api

sealed class Achievement {
    data class CounterAchievement(
        val id: String,
        val title: String,
        val result: Int,
    ) : Achievement()

    data class VeryFirstAchievement(
        val id: String,
        val title: String,
    ) : Achievement()
}
