package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.EquipTitleResult
import com.iurispraecepta.herolog.logic.TitleLogic
import com.iurispraecepta.herolog.logic.TitlePurchaseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TitleLogicTest {

    @Test
    fun buyTitle_success_deductsGoldAndAddsTitle() {
        val result = TitleLogic.buyTitle(
            gold = 500,
            ownedTitles = listOf("title_novice"),
            titleId = "title_expert",
            price = 200
        )

        assertTrue(result is TitlePurchaseResult.Success)
        val success = result as TitlePurchaseResult.Success
        assertEquals(300, success.newGold)
        assertEquals(listOf("title_novice", "title_expert"), success.newOwnedTitles)
    }

    @Test
    fun buyTitle_insufficientGold_returnsInsufficientGold() {
        val result = TitleLogic.buyTitle(
            gold = 100,
            ownedTitles = emptyList(),
            titleId = "title_expert",
            price = 200
        )

        assertEquals(TitlePurchaseResult.InsufficientGold, result)
    }

    @Test
    fun buyTitle_alreadyOwned_doesNotDeductGold() {
        val result = TitleLogic.buyTitle(
            gold = 500,
            ownedTitles = listOf("title_expert"),
            titleId = "title_expert",
            price = 200
        )

        assertEquals(TitlePurchaseResult.AlreadyOwned, result)
    }

    @Test
    fun claimAchievementTitle_addsIfNotOwned() {
        val result = TitleLogic.claimAchievementTitle(
            ownedTitles = listOf("title_novice"),
            titleId = "title_hero"
        )

        assertEquals(listOf("title_novice", "title_hero"), result)
    }

    @Test
    fun claimAchievementTitle_doesNotDuplicateIfAlreadyOwned() {
        val result = TitleLogic.claimAchievementTitle(
            ownedTitles = listOf("title_hero"),
            titleId = "title_hero"
        )

        assertEquals(listOf("title_hero"), result)
    }

    @Test
    fun equipTitle_successWhenOwned() {
        val result = TitleLogic.equipTitle(
            ownedTitles = listOf("title_hero"),
            titleId = "title_hero"
        )

        assertEquals(EquipTitleResult.Success("title_hero"), result)
    }

    @Test
    fun equipTitle_returnsNotOwnedWhenNotOwned() {
        val result = TitleLogic.equipTitle(
            ownedTitles = listOf("title_novice"),
            titleId = "title_hero"
        )

        assertEquals(EquipTitleResult.NotOwned, result)
    }

    @Test
    fun equipTitle_nullAlwaysSucceedsToUnequip() {
        val result = TitleLogic.equipTitle(
            ownedTitles = emptyList(),
            titleId = null
        )

        assertEquals(EquipTitleResult.Success(null), result)
    }
}
