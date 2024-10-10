package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Prayer
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.fighter.Fighter
import kotlin.random.Random

class PrayAtAltar(script: Fighter) : Leaf<Fighter>(script, "PrayAtAltar") {
	override fun execute() {
		val altar = Objects.stream().type(GameObject.Type.INTERACTIVE).action("Pray-at").nearest().first()
		val walkableTile = altar.getWalkableNeighbor(checkForWalls = false)
		if (walkableTile == null) {
			script.logger.info("Can't find walkable tile near altar...")
			return
		}

		if (!walkableTile.reachable()) {
			val door = Objects.stream(walkableTile, 10, GameObject.Type.BOUNDARY).name("Door").action("Open")
				.nearest(walkableTile).first()
			if (door.valid()) {
				if (walkAndInteract(door, "Open")) {
					waitForDistance(door) { walkableTile.reachable() }
				}
			} else if (LocalPathFinder.findWalkablePath(walkableTile).traverse()) {
				waitFor { walkableTile.reachable() }
			}
		} else if (walkAndInteract(altar, "Pray-at")) {
			if (waitForDistance(altar) { Prayer.prayerPoints() >= script.nextAltarPrayRestore }) {
				script.nextAltarPrayRestore = Random.nextInt(5, 15)
				script.logger.info("Prayed at altar setting nextPrayRestore=${script.nextAltarPrayRestore}")
			}
		}
	}


}