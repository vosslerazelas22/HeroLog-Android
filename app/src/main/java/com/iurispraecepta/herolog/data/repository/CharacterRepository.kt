package com.iurispraecepta.herolog.data.repository

import com.iurispraecepta.herolog.data.dao.CharacterStateDao
import com.iurispraecepta.herolog.data.entity.CharacterStateEntity
import com.iurispraecepta.herolog.model.CharacterState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CharacterRepository(
    private val characterStateDao: CharacterStateDao,
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) {
    suspend fun getCharacterState(): CharacterState? {
        val entity = characterStateDao.getState() ?: return null
        return runCatching {
            json.decodeFromString<CharacterState>(entity.jsonPayload)
        }.getOrNull()
    }

    suspend fun saveCharacterState(state: CharacterState) {
        val jsonPayload = json.encodeToString(state)
        val entity = CharacterStateEntity(
            id = 0,
            jsonPayload = jsonPayload,
            updatedAt = System.currentTimeMillis()
        )
        characterStateDao.saveState(entity)
    }

    suspend fun clearCharacterState() {
        characterStateDao.clearState()
    }
}
