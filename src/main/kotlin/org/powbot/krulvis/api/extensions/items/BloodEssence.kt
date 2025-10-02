package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Bank

object BloodEssence : Item {
	val ACTIVE_ID = 26392
	val INACTIVE_ID = 26390
	override val ids: IntArray = intArrayOf(ACTIVE_ID, INACTIVE_ID)
	override val itemName: String = "Blood Essence"
	override val stackable: Boolean = false

	var hasActive = false
	fun isActive(): Boolean = getInventoryId() == ACTIVE_ID

	fun activate(): Boolean {
		val invItem = getInvItem() ?: return false
		return invItem.id == ACTIVE_ID || invItem.interact("Activate")
	}

	override fun hasWith(): Boolean = getCount() > 0

	override fun getCount(countNoted: Boolean): Int = getInventoryCount()

	override fun withdrawExact(amount: Int, worse: Boolean, wait: Boolean): Boolean {
		return super.withdrawExact(amount, false, wait)
	}

	override fun getBankId(worse: Boolean): Int {
		val bankItems = Bank.stream().filterItems().filtered { it.stack > 0 }.toList()
		return bankItems.firstOrNull { it.id == ACTIVE_ID }?.id ?: bankItems.firstOrNull()?.id ?: -1
	}

	fun forId(id: Int): BloodEssence? = if (ids.contains(id)) this else null
}