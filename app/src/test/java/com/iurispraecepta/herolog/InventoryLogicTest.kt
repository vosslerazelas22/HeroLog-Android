package com.iurispraecepta.herolog

import com.iurispraecepta.herolog.logic.InventoryLogic
import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.InventoryItem
import com.iurispraecepta.herolog.model.Rarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryLogicTest {

    private val sampleItem1 = InventoryItem(
        id = "sword_1",
        name = "Espada de Ferro",
        emoji = "⚔️",
        buff = BuffType.UnwaveringSword,
        price = 101,
        desc = "Uma espada simples.",
        isEquipment = true,
        rarity = Rarity.Comum
    )

    private val sampleItem2 = InventoryItem(
        id = "shield_1",
        name = "Escudo de Madeira",
        emoji = "🛡️",
        buff = BuffType.RuneFortune,
        price = 200,
        desc = "Um escudo básico.",
        isEquipment = true,
        rarity = Rarity.Comum
    )

    private val samplePotion = InventoryItem(
        id = "potion_1",
        name = "Poção de Foco",
        emoji = "🧪",
        buff = BuffType.FocusElixir,
        price = 100,
        desc = "Restaura o foco.",
        isEquipment = false,
        rarity = Rarity.Comum
    )

    @Test
    fun equipItem_emptySlot_removesItemFromInventoryAndEquips() {
        val initialInventory = listOf(sampleItem1, samplePotion)
        val result = InventoryLogic.equipItem(
            inventory = initialInventory,
            equippedEquipment = null,
            item = sampleItem1,
            slotIdx = 0
        )

        assertEquals(1, result.inventory.size)
        assertEquals(samplePotion, result.inventory[0])
        assertEquals(3, result.equippedEquipment.size)
        assertEquals(sampleItem1, result.equippedEquipment[0])
        assertNull(result.equippedEquipment[1])
        assertNull(result.equippedEquipment[2])
    }

    @Test
    fun equipItem_occupiedSlot_returnsPreviousItemToInventory() {
        val initialInventory = listOf(sampleItem2)
        val initialEquipped = listOf(sampleItem1, null, null)

        val result = InventoryLogic.equipItem(
            inventory = initialInventory,
            equippedEquipment = initialEquipped,
            item = sampleItem2,
            slotIdx = 0
        )

        // sampleItem2 is removed from inventory, sampleItem1 is returned to inventory
        assertEquals(1, result.inventory.size)
        assertEquals(sampleItem1, result.inventory[0])
        assertEquals(sampleItem2, result.equippedEquipment[0])
    }

    @Test
    fun unequipItem_emptySlot_returnsUnchanged() {
        val initialInventory = listOf(samplePotion)
        val initialEquipped = listOf<InventoryItem?>(null, null, null)

        val result = InventoryLogic.unequipItem(
            inventory = initialInventory,
            equippedEquipment = initialEquipped,
            slotIdx = 0
        )

        assertEquals(initialInventory, result.inventory)
        assertEquals(initialEquipped, result.equippedEquipment)
    }

    @Test
    fun unequipItem_occupiedSlot_clearsSlotAndReturnsItemToInventory() {
        val initialInventory = listOf(samplePotion)
        val initialEquipped = listOf(sampleItem1, null, null)

        val result = InventoryLogic.unequipItem(
            inventory = initialInventory,
            equippedEquipment = initialEquipped,
            slotIdx = 0
        )

        assertEquals(2, result.inventory.size)
        assertTrue(result.inventory.contains(sampleItem1))
        assertTrue(result.inventory.contains(samplePotion))
        assertNull(result.equippedEquipment[0])
    }

    @Test
    fun activeBuffs_filtersOnlyRelevantBuffTypes() {
        val doubleLoot = InventoryItem("b1", "Double Loot", "🎲", BuffType.DoubleLoot, 100, "desc")
        val focusElixir = InventoryItem("b2", "Elixir", "🧪", BuffType.FocusElixir, 100, "desc")
        val pixelOwl = InventoryItem("b3", "Coruja", "🦉", BuffType.PixelOwl, 100, "desc", isEquipment = true)
        val inventory = listOf(doubleLoot, focusElixir, pixelOwl)

        val result = InventoryLogic.activeBuffs(inventory)

        assertEquals(2, result.size)
        assertTrue(result.contains(doubleLoot))
        assertTrue(result.contains(focusElixir))
        assertTrue(!result.contains(pixelOwl))
    }

    @Test
    fun sellItem_equipmentWithOddPrice_usesFloorHalfPriceAndRemovesFromInventory() {
        val initialInventory = listOf(sampleItem1, samplePotion)

        val (updatedInventory, price) = InventoryLogic.sellItem(initialInventory, sampleItem1)

        // sampleItem1 price = 101. floor(101 * 0.5) = 50
        assertEquals(50, price)
        assertEquals(1, updatedInventory.size)
        assertEquals(samplePotion, updatedInventory[0])
    }

    @Test
    fun calculateSellPrice_agreesWithSellItem_forEquipmentAndNonEquipment() {
        val equipCalculatedPrice = InventoryLogic.calculateSellPrice(sampleItem1)
        val (_, equipSellPrice) = InventoryLogic.sellItem(listOf(sampleItem1), sampleItem1)
        assertEquals(50, equipCalculatedPrice)
        assertEquals(equipSellPrice, equipCalculatedPrice)

        val nonEquipCalculatedPrice = InventoryLogic.calculateSellPrice(samplePotion)
        val (_, nonEquipSellPrice) = InventoryLogic.sellItem(listOf(samplePotion), samplePotion)
        assertEquals(50, nonEquipCalculatedPrice)
        assertEquals(nonEquipSellPrice, nonEquipCalculatedPrice)
    }

    @Test
    fun sellItem_nonEquipment_sellsForFixed50() {
        val initialInventory = listOf(samplePotion)

        val (updatedInventory, price) = InventoryLogic.sellItem(initialInventory, samplePotion)

        assertEquals(50, price)
        assertTrue(updatedInventory.isEmpty())
    }

    @Test
    fun discardItem_removesItemWithoutOtherEffects() {
        val initialInventory = listOf(sampleItem1, samplePotion)

        val updatedInventory = InventoryLogic.discardItem(initialInventory, sampleItem1)

        assertEquals(1, updatedInventory.size)
        assertEquals(samplePotion, updatedInventory[0])
    }
}
