package com.iurispraecepta.herolog.logic

import com.iurispraecepta.herolog.model.BuffType
import com.iurispraecepta.herolog.model.InventoryItem
import kotlin.math.floor

data class InventoryAndEquipment(
    val inventory: List<InventoryItem>,
    val equippedEquipment: List<InventoryItem?>
)

object InventoryLogic {
    fun addItemToInventory(inventory: List<InventoryItem>, item: InventoryItem): List<InventoryItem> {
        return inventory + item
    }

    fun equipItem(
        inventory: List<InventoryItem>,
        equippedEquipment: List<InventoryItem?>?,
        item: InventoryItem,
        slotIdx: Int
    ): InventoryAndEquipment {
        val currentEquipped = (equippedEquipment ?: listOf(null, null, null)).toMutableList()
        while (currentEquipped.size <= slotIdx) {
            currentEquipped.add(null)
        }

        val updatedInventory = inventory.filter { it.id != item.id }.toMutableList()
        val existingItem = currentEquipped.getOrNull(slotIdx)
        if (existingItem != null) {
            updatedInventory.add(existingItem)
        }
        currentEquipped[slotIdx] = item

        return InventoryAndEquipment(
            inventory = updatedInventory,
            equippedEquipment = currentEquipped
        )
    }

    fun unequipItem(
        inventory: List<InventoryItem>,
        equippedEquipment: List<InventoryItem?>?,
        slotIdx: Int
    ): InventoryAndEquipment {
        val currentEquipped = (equippedEquipment ?: listOf(null, null, null)).toMutableList()
        val item = currentEquipped.getOrNull(slotIdx)
            ?: return InventoryAndEquipment(inventory, currentEquipped)

        currentEquipped[slotIdx] = null
        val updatedInventory = inventory + item

        return InventoryAndEquipment(
            inventory = updatedInventory,
            equippedEquipment = currentEquipped
        )
    }

    fun calculateSellPrice(item: InventoryItem): Int {
        return if (item.isEquipment == true) {
            floor(item.price * 0.5).toInt()
        } else {
            50
        }
    }

    fun sellItem(inventory: List<InventoryItem>, item: InventoryItem): Pair<List<InventoryItem>, Int> {
        val sellingPrice = calculateSellPrice(item)
        val updatedInventory = inventory.filter { it.id != item.id }
        return Pair(updatedInventory, sellingPrice)
    }

    fun discardItem(inventory: List<InventoryItem>, item: InventoryItem): List<InventoryItem> {
        return inventory.filter { it.id != item.id }
    }

    private val ACTIVE_BUFF_TYPES = setOf(
        BuffType.DoubleLoot,
        BuffType.FocusElixir,
        BuffType.CrystalClarity,
        BuffType.RuneFortune,
        BuffType.StreakShield
    )

    // Fonte real: App.tsx filtra gameState.inventory.filter(i => ['DoubleLoot', 'FocusElixir',
    // 'CrystalClarity', 'RuneFortune', 'StreakShield'].includes(i.buff)) para popular activeBuffs
    // do CharacterScreen.
    fun activeBuffs(inventory: List<InventoryItem>): List<InventoryItem> {
        return inventory.filter { it.buff in ACTIVE_BUFF_TYPES }
    }
}
