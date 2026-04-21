package com.darts.assistant.game

import com.darts.assistant.bot.BotPlayer

class GameEngine(private val config: GameConfig) {

    val players: List<Player> = config.players.map { it.copy(score = config.startingPoints) }
    private val throwHistory = ArrayDeque<HistoryEntry>()

    var currentPlayerIndex = 0
        private set
    var gameState = GameState.PLAYING
        private set

    private var currentTurnThrows = mutableListOf<TurnThrow>()
    private var currentTurnScored = 0
    private var gameStartTime = System.currentTimeMillis()

    val currentPlayer get() = players[currentPlayerIndex]
    val dartsInTurn get() = currentTurnThrows.size
    val canThrow get() = currentTurnThrows.size < 3 && gameState == GameState.PLAYING

    data class HistoryEntry(
        val playerIndex: Int,
        val throws: List<TurnThrow>,
        val scoreBefore: Int
    )

    fun throwDart(value: Int, multiplier: Int): ThrowResult {
        if (!canThrow) return ThrowResult.NOT_ALLOWED

        val throwVal = TurnThrow(value, multiplier)
        val newScore = currentPlayer.score - throwVal.score

        return when {
            newScore < 0 -> {
                bust()
                ThrowResult.BUST
            }
            newScore == 1 -> {
                bust()
                ThrowResult.BUST
            }
            newScore == 0 -> {
                currentTurnThrows.add(throwVal)
                currentTurnScored += throwVal.score
                applyTurn()
                checkWin()
            }
            else -> {
                currentTurnThrows.add(throwVal)
                currentTurnScored += throwVal.score
                currentPlayer.dartsThrown++
                currentPlayer.totalScored += throwVal.score
                currentPlayer.score = newScore
                if (currentTurnThrows.size == 3) {
                    commitTurn()
                    ThrowResult.NEXT_PLAYER
                } else {
                    ThrowResult.CONTINUE
                }
            }
        }
    }

    private fun bust() {
        throwHistory.addLast(HistoryEntry(currentPlayerIndex, currentTurnThrows.toList(), currentPlayer.score + currentTurnScored))
        currentPlayer.dartsThrown += currentTurnThrows.size + 1
        currentTurnThrows.clear()
        currentTurnScored = 0
        advancePlayer()
    }

    private fun applyTurn() {
        currentPlayer.dartsThrown += currentTurnThrows.size
        currentPlayer.totalScored += currentTurnScored
        currentPlayer.score = 0
    }

    private fun checkWin(): ThrowResult {
        currentPlayer.legsWon++
        val legsNeeded = config.legs
        if (currentPlayer.legsWon >= legsNeeded) {
            currentPlayer.setsWon++
            players.forEach { it.legsWon = 0; it.score = config.startingPoints }
            val setsNeeded = config.sets
            if (currentPlayer.setsWon >= setsNeeded) {
                gameState = GameState.GAME_WON
                return ThrowResult.GAME_WON
            }
            gameState = GameState.SET_WON
            return ThrowResult.SET_WON
        }
        players.forEach { it.score = config.startingPoints }
        gameState = GameState.LEG_WON
        return ThrowResult.LEG_WON
    }

    fun continueAfterLegOrSet() {
        currentTurnThrows.clear()
        currentTurnScored = 0
        gameState = GameState.PLAYING
        advancePlayer()
    }

    private fun commitTurn() {
        throwHistory.addLast(HistoryEntry(currentPlayerIndex, currentTurnThrows.toList(), currentPlayer.score + currentTurnScored))
        currentTurnThrows.clear()
        currentTurnScored = 0
        advancePlayer()
    }

    fun nextPlayer() {
        commitTurn()
    }

    private fun advancePlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
    }

    fun undoLastTurn() {
        if (throwHistory.isEmpty()) return
        val last = throwHistory.removeLast()
        val player = players[last.playerIndex]
        val scoredInTurn = last.throws.sumOf { it.score }
        player.score = last.scoreBefore
        player.dartsThrown = (player.dartsThrown - last.throws.size).coerceAtLeast(0)
        player.totalScored = (player.totalScored - scoredInTurn).coerceAtLeast(0)
        currentPlayerIndex = last.playerIndex
        currentTurnThrows.clear()
        currentTurnScored = 0
        gameState = GameState.PLAYING
    }

    fun getBotThrow(): TurnThrow = BotPlayer.calculateThrow(currentPlayer.score)

    fun getCheckoutHint(): String? {
        val score = currentPlayer.score
        return if (score in 2..170) com.darts.assistant.game.CheckoutHints.getHint(score) else null
    }

    fun getTurnScore(): Int = currentTurnScored
    fun getCurrentThrows(): List<TurnThrow> = currentTurnThrows.toList()
    fun getGameDurationSeconds(): Long = (System.currentTimeMillis() - gameStartTime) / 1000
}

enum class ThrowResult {
    CONTINUE, NEXT_PLAYER, BUST, LEG_WON, SET_WON, GAME_WON, NOT_ALLOWED
}
