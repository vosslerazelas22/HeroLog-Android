package com.iurispraecepta.herolog.logic

sealed class TitlePurchaseResult {
    data class Success(val newGold: Int, val newOwnedTitles: List<String>) : TitlePurchaseResult()
    object InsufficientGold : TitlePurchaseResult()
    object AlreadyOwned : TitlePurchaseResult()
}

sealed class EquipTitleResult {
    data class Success(val equippedTitle: String?) : EquipTitleResult()
    object NotOwned : EquipTitleResult()
}

object TitleLogic {
    fun buyTitle(gold: Int, ownedTitles: List<String>?, titleId: String, price: Int): TitlePurchaseResult {
        val currentOwned = ownedTitles ?: emptyList()
        if (currentOwned.contains(titleId)) {
            return TitlePurchaseResult.AlreadyOwned
        }
        if (gold < price) {
            return TitlePurchaseResult.InsufficientGold
        }
        val updatedOwned = currentOwned + titleId
        return TitlePurchaseResult.Success(
            newGold = gold - price,
            newOwnedTitles = updatedOwned
        )
    }

    fun claimAchievementTitle(ownedTitles: List<String>?, titleId: String): List<String> {
        val currentOwned = ownedTitles ?: emptyList()
        return if (!currentOwned.contains(titleId)) {
            currentOwned + titleId
        } else {
            currentOwned
        }
    }

    fun equipTitle(ownedTitles: List<String>?, titleId: String?): EquipTitleResult {
        if (titleId == null) {
            return EquipTitleResult.Success(null)
        }
        val currentOwned = ownedTitles ?: emptyList()
        return if (currentOwned.contains(titleId)) {
            EquipTitleResult.Success(titleId)
        } else {
            EquipTitleResult.NotOwned
        }
    }
}
