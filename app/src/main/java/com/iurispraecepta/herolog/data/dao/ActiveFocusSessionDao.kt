package com.iurispraecepta.herolog.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iurispraecepta.herolog.data.entity.ActiveFocusSessionEntity

@Dao
interface ActiveFocusSessionDao {
    @Query("SELECT * FROM active_focus_session WHERE id = 0 LIMIT 1")
    suspend fun getSession(): ActiveFocusSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(entity: ActiveFocusSessionEntity)

    @Query("DELETE FROM active_focus_session WHERE id = 0")
    suspend fun clearSession()
}
