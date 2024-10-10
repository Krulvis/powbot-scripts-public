package org.powbot.krulvis.api.extensions.requirements

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.extensions.items.*


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
		Potion.forId(id) ?: BloodEssence.forId(id) ?: ITeleportItem.getTeleportItem(id) ?: InventoryItem(id),
		amount, false, allowMore, countNoted
	)

	fun getCount(): Int {
		return (if (onlyBest) Inventory.stream().id(*item.ids).count(countNoted).toInt()
		else item.getInventoryCount(countNoted))
	}

	override fun meets(): Boolean {
		val count = getCount()
		return if (allowLess) count >= minAmount else if (allowMore) count >= amount else count == amount
	}

	override fun toString(): String =
		"InventoryRequirement(name=${item.itemName}, amount=$amount, minAmount=${minAmount}, allowLess=${allowLess}, type=${item.javaClass.simpleName})"


	companion object {
		fun forOption(option: Map<Int, Int>) =
			option.map { InventoryRequirement(it.key, it.value, allowMore = it.value > 28) }
	}
}