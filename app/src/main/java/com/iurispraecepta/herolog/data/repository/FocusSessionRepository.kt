package com.iurispraecepta.herolog.data.repository

import com.iurispraecepta.herolog.data.dao.ActiveFocusSessionDao
import com.iurispraecepta.herolog.data.entity.ActiveFocusSessionEntity
import com.iurispraecepta.herolog.logic.focus.PersistedFocusSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FocusSessionRepository(
    private val dao: ActiveFocusSessionDao,
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
) {
    suspend fun getSession(): PersistedFocusSession? {
        val entity = dao.getSession() ?: return null
        return runCatching {
            json.decodeFromString<PersistedFocusSession>(entity.jsonPayload)
        }.getOrNull()
    }

    suspend fun saveSession(session: PersistedFocusSession) {
        val jsonPayload = json.encodeToString(session)
        val entity = ActiveFocusSessionEntity(
            id = 0,
            jsonPayload = jsonPayload,
            updatedAt = System.currentTimeMillis()
        )
        dao.saveSession(entity)
    }

    suspend fun clearSession() {
        dao.clearSession()
    }
}
