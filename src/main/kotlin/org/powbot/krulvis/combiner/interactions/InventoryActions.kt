package org.powbot.krulvis.combiner.interactions

import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.krulvis.combiner.interactions.GameActions.isUsingItem
import org.powbot.krulvis.combiner.interactions.GameActions.itemUsing
import kotlin.math.abs

object InventoryActions {

    /**
     * @return the first item matching a name or the item closest to the "using" item
     */
    fun InventoryItemActionEvent.getItem(): Item? {
        val itemName = if (name.contains("->")) name.substring(name.indexOf("-> ") + 3) else name
        val item = if (isUsingItem()) {
            val prevItemIndex = Inventory.stream().name(itemUsing()).first().inventoryIndex
            val allRelevantItemsSorted = Inventory.stream().name(itemName).toList()
                .map { abs(it.inventoryIndex - prevItemIndex) to it }.sortedBy { it.first }
            val distance = allRelevantItemsSorted.firstOrNull()?.first ?: return null
            allRelevantItemsSorted.filter { it.first == distance }.maxByOrNull { it.second.inventoryIndex }?.second
        } else
            Inventory.stream().name(itemName).firstOrNull()
        return item
    }

}


