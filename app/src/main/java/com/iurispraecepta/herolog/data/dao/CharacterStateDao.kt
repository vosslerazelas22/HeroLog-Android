package com.iurispraecepta.herolog.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iurispraecepta.herolog.data.entity.CharacterStateEntity

@Dao
interface CharacterStateDao {
    @Query("SELECT * FROM character_state WHERE id = 0 LIMIT 1")
    suspend fun getState(): CharacterStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveState(entity: CharacterStateEntity)

    @Query("DELETE FROM character_state WHERE id = 0")
    suspend fun clearState()
}
