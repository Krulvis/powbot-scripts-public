package org.powbot.krulvis.api.extensions.items

object BloodEssence : Item {
	val ACTIVE_ID = 26392
	val INACTIVE_ID = 26390
	override val ids: IntArray = intArrayOf(ACTIVE_ID, INACTIVE_ID)
	override val itemName: String = "Blood Essence"
	override val stackable: Boolean = false

	fun isActive(): Boolean = getInventoryId() == ACTIVE_ID

	fun activate(): Boolean {
		val invItem = getInvItem() ?: return false
		return invItem.id == ACTIVE_ID || invItem.interact("Activate")
	}

	override fun hasWith(): Boolean = getCount() > 0

	override fun getCount(countNoted: Boolean): Int = getInventoryCount()

	fun forId(id: Int): BloodEssence? = if (ids.contains(id)) this else null
}