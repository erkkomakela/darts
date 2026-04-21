package com.darts.assistant.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY date DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()
}
