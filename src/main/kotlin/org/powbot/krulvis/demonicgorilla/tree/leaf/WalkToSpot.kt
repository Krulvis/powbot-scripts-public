package org.powbot.krulvis.demonicgorilla.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class WalkToSpot(script: DemonicGorilla) : Leaf<DemonicGorilla>(script, "Walking to spot") {
	private val royalSeedTeleportDestination = Tile(2465, 3495, 0)
	private val battlements = Tile(2007, 5588, 0)
	override fun execute() {
		Chat.clickContinue()
		val spot = script.centerTile
		val nearby = script.nearbyMonsters()
		if (nearby.none { it.reachable() } || (spot.distance() > 15)) {
			if (spot.distance() <= 10 && spot.reachable()) {
				Movement.step(spot, 0)
				return
			}
			if (royalSeedTeleportDestination.distance() < 50 || battlements.distance() < 50) {
				script.seedPodTeleport.executed = true
			}
			val path = LocalPathFinder.findPath(spot)
			if (path.isNotEmpty()) {
				script.seedPodTeleport.executed = false
				path.traverse()
			} else if (script.seedPodTeleport.execute()) {
				script.aggressionTimer.reset()
				Movement.builder(spot).setWalkUntil { spot.distance() < 25 }.move()
			}
		}
	}
}