package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.DepositBox
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.FailureReason
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.mobile.script.ScriptManager

class Walk(script: Woodcutter) : Leaf<Woodcutter>(script, "Walk to Trees") {
	override fun execute() {
		val locs = script.trees
		Bank.close()
		DepositBox.close()
		script.chopDelay.forceFinish()
		if (locs.isEmpty()) {
			script.logger.info("Script requires at least 1 Tree GameObject set in the Configuration")
			ScriptManager.stop()
		} else {
			val nearestTile = locs.minByOrNull { it.distance() } ?: return
			val tile = nearestTile.getWalkableNeighbor(diagonalTiles = true, checkForWalls = false) ?: nearestTile
			if (script.forceWeb) {
				val webWalking = walkWebTo(tile)
				if(!webWalking.success){
					script.logger.info("Walking to tile=$tile failed with reason=${webWalking.failureReason}")
					if(webWalking.failureReason == null || webWalking.failureReason == FailureReason.NoPath){
						val centerPoint = Tile(locs.sumOf { it.x } / locs.size, locs.sumOf { it.y } / locs.size, locs.first().floor)
						script.logger.info("Walking to centerpoint=$centerPoint")
						walkWebTo(centerPoint)
					}
				}
				return
			}
			if (tile.distance() < 15) {
				Movement.step(tile)
				waitFor(long()) { tile.distance() < 5 }
			} else if (tile.floor == 1 && script.redwoods && me.floor() == 0) { //Climb ladder to access Redwood trees
				val ladder = Objects.stream().name("Rope ladder").action("Climb-up").nearest().first()
				if (walkAndInteract(ladder, "Climb-up")) {
					waitForDistance(ladder) { me.floor() == 1 }
				}
			} else {
				Movement.walkTo(tile.getWalkableNeighbor(allowSelf = false, diagonalTiles = false, checkForWalls = false))
			}
		}
	}


	private fun walkWebTo(tile:Tile) = Movement.builder(tile).setForceWeb(true).move()

}