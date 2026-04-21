package com.darts.assistant.game

import java.io.Serializable

data class Player(
    val name: String,
    val isBot: Boolean = false,
    var score: Int = 0,
    var dartsThrown: Int = 0,
    var totalScored: Int = 0,
    var legsWon: Int = 0,
    var setsWon: Int = 0
) : Serializable {
    val average: Double
        get() = if (dartsThrown == 0) 0.0 else (totalScored.toDouble() / dartsThrown) * 3
}

data class TurnThrow(
    val value: Int,
    val multiplier: Int
) {
    val score: Int get() = value * multiplier
    override fun toString(): String = when (multiplier) {
        2 -> "D$value"
        3 -> "T$value"
        else -> "$value"
    }
}

data class GameConfig(
    val startingPoints: Int,
    val sets: Int,
    val legs: Int,
    val players: List<Player>
) : Serializable

enum class GameState { PLAYING, LEG_WON, SET_WON, GAME_WON }
