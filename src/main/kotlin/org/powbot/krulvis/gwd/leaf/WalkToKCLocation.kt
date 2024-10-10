package org.powbot.krulvis.gwd.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.gwd.GWD
import org.powbot.krulvis.gwd.GWDScript

class WalkToKCLocation<S>(script: S) : Leaf<S>(script, "Walk to KC location") where S : GWDScript<S> {
	override fun execute() {
		val kcTile = script.god.kcLocation
		if (script.god == GWD.God.Saradomin && kcTile.distance() > 20) {
			val floor = me.tile().floor
			script.logger.info("Going to Saradomin, on floor=$floor")
			if (floor == 2) {
				val rock =
					Objects.stream(Tile(2914, 5300, 2), GameObject.Type.INTERACTIVE).name("Rock").action("Climb-down")
						.first()
				script.logger.info("Rock on floor 2 = $rock")
				if (!rock.valid()) {
					script.logger.info("Cannot find rock...")
					return
				} else if (rock.distance() > 15) {
					Movement.step(rock.tile)
				} else if (walkAndInteract(rock, "Climb-down")) {
					waitForDistance(rock) { me.tile().floor == 1 }
				}
			} else if (floor == 1) {
				val rock =
					Objects.stream(Tile(2920, 5275, 1), GameObject.Type.INTERACTIVE).name("Rock").action("Climb-down")
						.first()
				script.logger.info("Rock on floor 1 = $rock")
				if (rock.valid() && walkAndInteract(rock, "Climb-down")) {
					waitForDistance(rock) { me.tile().floor == 0 }
				}
			}
		} else {

			Movement.walkTo(script.god.kcLocation)
		}
	}
}