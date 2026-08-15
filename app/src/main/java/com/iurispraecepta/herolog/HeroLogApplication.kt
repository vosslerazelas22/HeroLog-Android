package com.iurispraecepta.herolog

import android.app.Application
import androidx.room.Room
import com.iurispraecepta.herolog.data.database.HeroLogDatabase
import com.iurispraecepta.herolog.data.repository.CharacterRepository
import com.iurispraecepta.herolog.data.repository.FocusSessionRepository

class HeroLogApplication : Application() {
    val database: HeroLogDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            HeroLogDatabase::class.java,
            "herolog.db"
        ).fallbackToDestructiveMigration().build()
    }

    val characterRepository: CharacterRepository by lazy {
        CharacterRepository(database.characterStateDao())
    }

    val focusSessionRepository: FocusSessionRepository by lazy {
        FocusSessionRepository(database.activeFocusSessionDao())
    }
}
