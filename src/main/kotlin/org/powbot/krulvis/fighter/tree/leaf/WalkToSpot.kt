package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.fighter.Fighter

class WalkToSpot(script: Fighter) : Leaf<Fighter>(script, "Walking to spot") {

	override fun execute() {
		Chat.clickContinue()
		val npcTeleport = script.monsterTeleport
		val spot = script.centerTile
		val nearby = script.nearbyMonsters()
		if (nearby.none { it.reachable() } || (spot.distance() > if (script.useSafespot) script.safespotRadius else script.killRadius)) {
			if (spot.distance() <= 10 && spot.reachable()) {
				Movement.step(spot, 0)
				return
			}
			val path = LocalPathFinder.findPath(spot)
			if (path.isNotEmpty()) {
				script.monsterTeleport.executed = false
				path.traverseUntilReached(0.0)
			} else if (npcTeleport.execute()) {
				script.logger.info("Walking to spot after npcTeleport executed successfully")
				script.aggressionTimer.reset()
				Movement.builder(spot).setUseTeleports(script.useTeleportsToMonsters).move()
			}
		}
	}
}