package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.stripNumbersAndCharges
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("EquipmentItem")

class EquipmentItem(override val id: Int, override val slot: Equipment.Slot) : IEquipmentItem {
	override val ids: IntArray = intArrayOf(id)
	override val itemName: String by lazy { ItemLoader.lookup(id)!!.name().stripNumbersAndCharges() }
	override val stackable: Boolean by lazy { ItemLoader.lookup(id)!!.stackable() }
}

interface IEquipmentItem : Item {

	val slot: Equipment.Slot

	override fun hasWith(): Boolean = inInventory() || inEquipment()

	override fun getCount(countNoted: Boolean): Int {
		return getInventoryCount(false) + getEquipmentCount()
	}

	fun withdrawAndEquip(stopIfOut: Boolean = false): Boolean {
		if (inEquipment()) {
			return true
		} else if (!inInventory()) {
			if (Bank.withdrawModeNoted(false) && inBank()
				&& withdrawExact(1, true)
			) {
				waitFor(5000) { inInventory() }
			} else if (!inBank() && stopIfOut) {
				ScriptManager.script()!!.logger.warn("Stopping script due to being out of: $itemName")
				ScriptManager.stop()
			}
		}
		if (inInventory()) {
			logger.info("withdrawAndEquip: $this, inInventory=true")
			return equip(true)
		}
		return inEquipment()
	}

	fun equip(wait: Boolean = true): Boolean {
		if (inEquipment()) return true
		if (inInventory() && (Bank.opened() || Game.tab(Game.Tab.INVENTORY))) {
			val item = Inventory.stream().nameContains(itemName).first()
			val actions = item.actions()
			val action = actions.firstOrNull { it in listOf("Wear", "Wield", "Equip") }
			ScriptManager.script()!!.logger.info("Equipping $itemName action=$action, actions=[${actions.joinToString()}]")
			if (if (action == null || !wait) item.click() else item.interact(action)) {
				if (wait) waitFor(2000) { inEquipment() } else return true
			}
		} else {
			logger.info("Equipping $itemName is not in inventory, ids=[${ids.joinToString()}]")
		}
		return inEquipment()
	}

	fun dequip(): Boolean {
		if (!inEquipment()) {
			return true
		}
		val equipped = Equipment.stream().nameContains(itemName).first()
		return equipped.click()
	}
}