package com.example.gymstats.model

data class Routine(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val dayOfWeek: String = "",
    val createdAt: Long = System.currentTimeMillis()
)