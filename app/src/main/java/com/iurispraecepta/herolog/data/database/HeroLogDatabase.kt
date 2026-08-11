package com.iurispraecepta.herolog.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iurispraecepta.herolog.data.dao.CharacterStateDao
import com.iurispraecepta.herolog.data.entity.CharacterStateEntity

@Database(entities = [CharacterStateEntity::class], version = 1, exportSchema = false)
abstract class HeroLogDatabase : RoomDatabase() {
    abstract fun characterStateDao(): CharacterStateDao
}
