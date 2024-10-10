package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.AlchemyRoom
import kotlin.random.Random

class CastHighAlch(script: MTA) : Leaf<MTA>(script, "Casting high alch") {
	override fun execute() {
		val item = AlchemyRoom.bestItem.inventoryItem()
		val coins = Inventory.stream().id(995).count(true)
		if (castingDelay.isFinished()) {
			if (Magic.Spell.HIGH_ALCHEMY.cast()) {
				script.logger.info("Selected HA")
				waitFor { casting() && Game.tab() == Game.Tab.INVENTORY }
				sleep(150)
			}
			val invOpen = Game.tab() == Game.Tab.INVENTORY
			script.logger.info("Game.tab() = ${Game.tab()}, invOpen=${invOpen}")
			if (invOpen && item.interact("Cast")) {
				if (waitFor { Game.tab() == Game.Tab.MAGIC }) {
					castingDelay.reset(Random.nextInt(1700, 1900))
					script.logger.info("Done casting HA")
				} else {
					script.logger.info("Failed to cast HA")
				}
			} else {
				script.logger.info("Failed to click item")
			}
		}

	}

	var castingDelay = Timer(1500)

	private fun casting() = Magic.magicspell() == Magic.Spell.HIGH_ALCHEMY
}