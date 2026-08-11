package com.iurispraecepta.herolog.logic.focus

/**
 * Port do cálculo de cooldown de Masmorra, auditado a partir da fonte real de `App.tsx`
 * (09/08/2026): `checkCooldown()` + `formatDungeonCooldown()`.
 *
 * DIVERGÊNCIA CONSCIENTE APROVADA POR BRUNO (09/08/2026): no React, `lastDungeonClearedTime` é
 * `useState<number>(0)` — não persiste em lugar nenhum, então um reload de página "reseta" o
 * cooldown (elapsed vira gigantesco a partir de 0, sempre >= cooldownTotal). O Android vai
 * **persistir de verdade** esse campo (ex. em `CharacterState`/Room) — este arquivo não decide
 * onde/como persistir, só recebe `lastClearedAtMs` já pronto de quem chama, exatamente como
 * `referenceDate` injetável em `QuestLogic`/`FocusApplyLogic`.
 */

private const val DUNGEON_COOLDOWN_TOTAL_MS = 2 * 60 * 60 * 1000L // 2 horas, fonte real

/**
 * Fonte real (`checkCooldown` em `App.tsx`):
 * ```
 * elapsed = Date.now() - lastDungeonClearedTime
 * cooldownTotal = 2h
 * if (elapsed < cooldownTotal) remaining = cooldownTotal - elapsed else remaining = 0
 * ```
 * `lastClearedAtMs = 0L` (ou qualquer valor tal que `elapsed >= cooldownTotal`) retorna 0 —
 * mesmo comportamento de "nunca limpou a masmorra ainda" do React, só que aqui por causa de um
 * valor inicial explícito, não por reset de reload (ver divergência consciente acima).
 */
fun resolveDungeonCooldownRemainingMs(
    lastClearedAtMs: Long,
    nowMs: Long = System.currentTimeMillis(),
): Long {
    val elapsed = nowMs - lastClearedAtMs
    return if (elapsed < DUNGEON_COOLDOWN_TOTAL_MS) {
        DUNGEON_COOLDOWN_TOTAL_MS - elapsed
    } else {
        0L
    }
}

/**
 * Fonte real (`formatDungeonCooldown` em `App.tsx`): formato `HH:MM:SS` zero-padded (NÃO é
 * "Xh Ym" — corrige o placeholder `formatDungeonCooldownStub` de `RaidModeSection.kt`, que era
 * só um stub de leiaute até esta fonte chegar).
 * ```
 * totalSecs = max(0, ceil(ms / 1000))
 * secs = totalSecs % 60
 * mins = floor(totalSecs / 60) % 60
 * hrs  = floor(totalSecs / 3600)
 * pad(n) = n.toString().padStart(2, '0')
 * return "${pad(hrs)}:${pad(mins)}:${pad(secs)}"
 * ```
 */
fun formatDungeonCooldown(remainingMs: Long): String {
    val totalSecs = maxOf(0L, Math.ceil(remainingMs / 1000.0).toLong())
    val secs = totalSecs % 60
    val mins = (totalSecs / 60) % 60
    val hrs = totalSecs / 3600

    fun pad(n: Long): String = n.toString().padStart(2, '0')

    return "${pad(hrs)}:${pad(mins)}:${pad(secs)}"
}
