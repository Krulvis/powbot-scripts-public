package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.magic.RunePouch
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Item.Companion.DIVINE_RUNE_POUCH
import org.powbot.krulvis.api.extensions.items.Item.Companion.RUNE_POUCH
import org.slf4j.LoggerFactory

object RunePouch : Item {

	val logger = LoggerFactory.getLogger(javaClass.simpleName)!!
	override val ids: IntArray = intArrayOf(RUNE_POUCH, DIVINE_RUNE_POUCH)
	override val itemName: String = "Rune pouch"
	override val stackable: Boolean = false

	override fun hasWith(): Boolean = inInventory()

	override fun getCount(countNoted: Boolean): Int = getInventoryCount(countNoted)

	fun runes() = RunePouch.runes().filterNot { it.first == Rune.NIL }

	const val ROOT_ID = 15
	fun isEmpty() = runes().none { it.first != Rune.NIL }
	fun depositRunes(): Boolean {
		if (isEmpty() || !open()) return true
		return Bank.opened() && depositComp().interact("Deposit runes") && waitFor { isEmpty() }
	}

	fun List<Pair<Rune, Int>>.runeCount(rune: Rune) = firstOrNull { it.first == rune }?.second ?: 0

	private fun isOpen() = depositComp().visible()

	fun open() =
		isOpen() || Inventory.stream().id(*ids).first().interact("Configure") && waitFor { isOpen() }

	fun close() = !isOpen() || Components.stream(ROOT_ID).action("Dismiss").first().interact("Dismiss")

	private fun depositComp() = Components.stream(ROOT_ID).action("Deposit runes").first()

	fun isRunePouch(item: Item) = item.ids.any { it in ids }

}