package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mta.MTA

class GoInside(script: MTA) : Leaf<MTA>(script, "Go Inside Room") {
	override fun execute() {
		val entrance = getEntrance()
		if (!entrance.valid()) {
			script.logger.info("entrance can't be found, looking for ${script.method}")
			Movement.walkTo(Tile(3363, 3317, 0))
		} else if (walkAndInteract(entrance, "Enter")) {
			waitForDistance(entrance) { script.room.inside() }
		}
	}

	private fun getEntrance() = Objects.stream(50).type(GameObject.Type.INTERACTIVE).nameContains(script.room.portalName).action("Enter").first()

}