package org.powbot.krulvis.api.extensions.items

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.uppercaseFirst
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.mobile.script.ScriptManager

enum class EssencePouch(val capacity: Int, val stopCapacity: Int, val perfectId: Int, val brokenId: Int) : Item {
	SMALL(3, 0, 5509, -2),
	MEDIUM(6, 2, 5510, 5511),
	LARGE(9, 5, 5512, 5513),
	GIANT(12, 8, 5514, 5515),
	COLOSSAL(40, 30, 26784, 26786),
	;

	override val itemName: String = "${name.uppercaseFirst()} pouch"

	override val ids: IntArray = intArrayOf(perfectId, brokenId)
	override val stackable: Boolean = false

	override fun hasWith(): Boolean = getInvItem() != null

	override fun getCount(countNoted: Boolean): Int = if (getInvItem() != null) 1 else 0
	override fun getNotedIds(): IntArray = intArrayOf(perfectId, brokenId)

	override fun getInvItem(worse: Boolean): org.powbot.api.rt4.Item? =
		Inventory.stream().nameContains(itemName).firstOrNull()

	var essenceCount: Int = 0
		set(value) {
			field = value.coerceIn(0, capacity)
		}

	fun emptied(): Boolean = essenceCount == 0
	fun filled(): Boolean = essenceCount == capacity
	fun fill(): Boolean {
		val pouch = getInvItem() ?: return true
		val invEssence = invEssenceCount()
		if (!pouch.valid()) return false
		if (pouch.interact("Fill")) {
			var newCount = -1
			waitFor(1200) {
				newCount = invEssenceCount()
				invEssence != newCount
			}
			essenceCount += if (invEssence == newCount) capacity else invEssence - newCount
			if (pouch.id == brokenId && newCount > 0 && invEssence != newCount && essenceCount <= stopCapacity) {
				Notifications.showNotification("Not using valid method of repairing pouches, stopping script")
				ScriptManager.stop()
			}
		} else {
			essenceCount = capacity
		}
		return filled()
	}

	fun empty(): Boolean {
		if (Inventory.isFull()) return false
		val pouch = getInvItem() ?: return false
		val startEssence = invEssenceCount()
		if (pouch.interact("Empty")) {
			var essenceCount = -1
			waitFor(1200) {
				essenceCount = invEssenceCount()
				essenceCount > startEssence
			}
			if (essenceCount == 0) {
				this.essenceCount = 0
				return true
			} else this.essenceCount -= essenceCount - startEssence
			return essenceCount > startEssence
		}
		return false
	}

	fun shouldRepair(): Boolean {
		val invItem = getInvItem() ?: return false
		return invItem.id != perfectId
	}

	companion object {
		val ids = values().flatMap { it.ids.toList() }.toIntArray()
		private val names = values().map { it.itemName }
		fun invEssenceCount() =
			Inventory.stream().id(Item.RUNE_ESSENCE, Item.PURE_ESSENCE, Item.DAEYALT_ESSENCE).count().toInt()

		fun inInventory() = values().filter { it.getInvItem() != null }
		fun inBank() = Bank.get { it.name() in names }

		fun forId(id: Int) = values().firstOrNull { it.ids.contains(id) }
	}
}