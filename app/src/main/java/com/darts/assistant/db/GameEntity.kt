package com.darts.assistant.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val gameMode: String,
    val startingPoints: Int,
    val winnerName: String,
    val playersSummary: String,
    val durationSeconds: Long
)
