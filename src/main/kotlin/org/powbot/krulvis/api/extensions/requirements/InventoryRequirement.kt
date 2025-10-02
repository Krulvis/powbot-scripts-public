package org.powbot.krulvis.api.extensions.requirements

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.extensions.items.*
import org.powbot.krulvis.api.extensions.items.teleports.ITeleportItem
import org.powbot.mobile.script.ScriptManager


open class InventoryRequirement(
    override val item: Item,
    override var amount: Int,
    private val onlyBest: Boolean = false,
    val allowMore: Boolean = false,
    val countNoted: Boolean = true
) : ItemRequirement {

    var minAmount = 1
    var allowLess = false

    constructor(id: Int, amount: Int, allowMore: Boolean = false, countNoted: Boolean = true) : this(
        Potion.forId(id) ?: BloodEssence.forId(id) ?: EssencePouch.forId(id) ?: ITeleportItem.getTeleportItem(id)
        ?: InventoryItem(id), amount, false, allowMore, countNoted
    )

    fun getCount(): Int {
        return if (onlyBest) {
            Inventory.stream().id(*item.ids).count(countNoted).toInt()
        } else {
            item.getInventoryCount(countNoted)
        }
    }

    override fun meets(): Boolean {
        val count = getCount()
        return if (allowLess) count >= minAmount else if (allowMore) count >= amount else count == amount
    }

    override fun toString(): String =
        "InventoryRequirement(name=${item.itemName}, id=${item.id}, amount=$amount, minAmount=${minAmount}, allowLess=${allowLess}, type=${item.javaClass.simpleName})"


    companion object {
        fun List<InventoryRequirement>.ids() = flatMap { it.item.ids.toList() }.toIntArray()
        fun forOption(option: Map<Int, Int>) =
            option.map { InventoryRequirement(it.key, it.value, allowMore = it.value > 28) }
    }
}