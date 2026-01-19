package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.Production.stoppedMaking
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.tithe.Data.EMPTY_CAN
import org.powbot.krulvis.tithe.Data.WATER_CAN_FULL
import org.powbot.krulvis.tithe.TitheFarmer

class Refill(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Refilling") {

	fun getWaterBarrel(): GameObject =
		Objects.stream(25).name("Water Barrel").nearest().first()

	override fun execute() {
		val waterBarrel = getWaterBarrel()
		waterBarrel.bounds(-32, 32, -64, 0, -32, 32)
		println("Barrel: present=${waterBarrel.valid()}")
		val emptyCan = Inventory.stream().firstOrNull { it.id in EMPTY_CAN until WATER_CAN_FULL }
		if (waterBarrel.valid()) {
			if (!stoppedMaking(WATER_CAN_FULL)) {
				println("Already filling water...")
				waitFor(long()) { Inventory.stream().noneMatch { item -> item.id() in EMPTY_CAN until WATER_CAN_FULL } }
			} else if (Game.tab(Game.Tab.INVENTORY) && walkAndInteract(
					waterBarrel,
					"Use",
					selectItem = emptyCan?.id ?: -1
				)
			) {
				waitFor(5000) { !stoppedMaking(WATER_CAN_FULL) }
			} else {
				debug("Failed to refill...")
			}
		}
	}
}