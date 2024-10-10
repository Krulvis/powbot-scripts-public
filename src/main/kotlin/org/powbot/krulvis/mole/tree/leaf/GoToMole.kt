package org.powbot.krulvis.mole.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.mole.GiantMole
import org.powbot.krulvis.mole.tree.branch.moleArea

class GoToMole(script: GiantMole) : Leaf<GiantMole>(script, "Going to mole") {
	private val parkTile = Tile(3001, 3376, 0)
	override fun execute() {
		if (parkTile.distance() > 15) {
			if (script.teleportToMole.execute())
				Movement.walkTo(parkTile)
		} else {
			script.teleportToMole.executed = false
			val hill = Objects.stream().name("Mole hill").action("Look-inside").nearest().first()
			if (hill.tile != me.tile()) {
				Movement.step(hill.tile, 0)
			} else {
				val spade = Inventory.stream().name("Spade").first()
				if (spade.interact("Dig")) {
					waitFor { moleArea.contains(me) }
				}
			}
		}
	}
}