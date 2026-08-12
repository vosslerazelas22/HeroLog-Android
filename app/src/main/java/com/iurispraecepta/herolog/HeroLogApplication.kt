package com.iurispraecepta.herolog

import android.app.Application
import androidx.room.Room
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository

class HeroLogApplication : Application() {
    val database: HeroLogDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            HeroLogDatabase::class.java,
            "herolog.db"
        ).build()
    }

    val characterRepository: CharacterRepository by lazy {
        CharacterRepository(database.characterStateDao())
    }
}
