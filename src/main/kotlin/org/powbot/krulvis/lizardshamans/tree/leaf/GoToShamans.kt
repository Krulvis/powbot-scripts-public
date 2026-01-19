package org.powbot.krulvis.lizardshamans.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.lizardshamans.Data.SHAMAN_AREAS
import org.powbot.krulvis.lizardshamans.Data.SLAYER_CAVE
import org.powbot.krulvis.lizardshamans.Data.SLAYER_CAVE_ENTRANCE
import org.powbot.krulvis.lizardshamans.LizardShamans

class GoToShamans(script: LizardShamans) : Leaf<LizardShamans>(script, "Go To Shamans") {

	override fun execute() {
		if (script.slayerTask) {
			getToSlayerCave()
		} else {
			getToRegularCave()
		}
	}

	private fun getToSlayerCave() {
		if (SLAYER_CAVE.loaded()) {
			val players = Players.stream().notLocalPlayer().toList()
			val area = SHAMAN_AREAS.firstOrNull { area -> players.none { area.contains(it) } }
			if (area == null) {
				//should hop because all killing area's are taken
			} else {
				val entrance = Objects.stream().type(GameObject.Type.INTERACTIVE).name("Crevice").action("Squeeze-through").nearest(area.centralTile).first()
				if (walkAndInteract(entrance, "Squeeze-through")) {
					waitForDistance(entrance) { area.contains(me) }
				}
			}
		} else if (SLAYER_CAVE_ENTRANCE.distance() > 10) {
			if (script.shamanTeleport.execute()) {
				Movement.walkTo(SLAYER_CAVE_ENTRANCE)
			}
		} else {
			val entrance = Objects.stream(Tile(1307, 3574, 0), GameObject.Type.INTERACTIVE).name("Lizardman lair").first()
			if (walkAndInteract(entrance, "Enter")) {
				waitForDistance(entrance) { SLAYER_CAVE.loaded() }
			}
		}
	}

	private fun getToRegularCave() {

	}
}