package com.iurispraecepta.herolog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_state")
data class CharacterStateEntity(
    @PrimaryKey val id: Int = 0,
    val jsonPayload: String,
    val updatedAt: Long = System.currentTimeMillis()
)
