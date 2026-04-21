package com.darts.assistant.bot

import com.darts.assistant.game.TurnThrow
import kotlin.random.Random

object BotPlayer {
    private val SKILL = 0.72

    fun calculateThrow(remainingScore: Int): TurnThrow {
        return when {
            remainingScore <= 0 -> TurnThrow(0, 1)
            remainingScore == 50 -> if (Random.nextDouble() < SKILL) TurnThrow(25, 2) else missedBull()
            remainingScore <= 40 && remainingScore % 2 == 0 -> attemptDouble(remainingScore / 2)
            remainingScore <= 170 -> attemptScoring(remainingScore)
            else -> TurnThrow(20, 3).let { applyMiss(it) }
        }
    }

    private fun attemptDouble(number: Int): TurnThrow {
        return if (Random.nextDouble() < SKILL * 0.85) {
            TurnThrow(number, 2)
        } else {
            val miss = Random.nextInt(3)
            when (miss) {
                0 -> TurnThrow(number, 1)
                1 -> if (number > 1) TurnThrow(number - 1, 2) else TurnThrow(1, 1)
                else -> TurnThrow(0, 1)
            }
        }
    }

    private fun attemptScoring(score: Int): TurnThrow {
        val target = when {
            score >= 60 -> TurnThrow(20, 3)
            score >= 40 -> TurnThrow(20, 2)
            score > 20 -> TurnThrow(score - (score % 2).coerceAtLeast(2), 1)
            else -> TurnThrow(score, 1)
        }
        return applyMiss(target)
    }

    private fun applyMiss(intended: TurnThrow): TurnThrow {
        if (Random.nextDouble() < SKILL) return intended
        val missType = Random.nextInt(4)
        return when (missType) {
            0 -> TurnThrow(intended.value, (intended.multiplier - 1).coerceAtLeast(1))
            1 -> TurnThrow((intended.value - 1).coerceAtLeast(1), intended.multiplier)
            2 -> TurnThrow((intended.value + 1).coerceAtMost(20), intended.multiplier)
            else -> TurnThrow(intended.value, 1)
        }
    }

    private fun missedBull(): TurnThrow {
        return if (Random.nextBoolean()) TurnThrow(25, 1) else TurnThrow(Random.nextInt(1, 6), 1)
    }
}
