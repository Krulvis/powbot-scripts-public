package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.Utils.walkAndInteract
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.miner.Miner
import org.powbot.mobile.script.ScriptManager

class WalkToSpot(script: Miner) : Leaf<Miner>(script, "Walking to spot") {

    fun walkToCamTorumMine() {
        script.tilesToCamTorumMine.traverse()
    }

    override fun execute() {
        val locs = script.rockLocations
        val allTop = script.rockLocations.all { script.inTopFloorAreas(it) }
        script.mineDelay.forceFinish()
        if (locs.isEmpty()) {
            script.logger.info("Script requires at least 1 rock location set in the Configuration")
            ScriptManager.stop()
        } else if (allTop
                && !script.inTopFloorAreas()
                && LocalPathFinder.findPath(Players.local().tile(), script.northOfLadder, true).isEmpty()
        ) {
            script.logger.info("Climbing ladder manually")
            val ladderGoingUp = Objects.stream().at(Tile(3755, 5673, 0)).name("Ladder").action("Climb").firstOrNull()
            ladderGoingUp?.bounds(-36, 22, -264, 0, 58, 62)
            if (ladderGoingUp != null && walkAndInteract(ladderGoingUp, "Climb")) {
                waitFor { script.northOfLadder.distance() <= 1 }
            }
        } else {
            var loc = locs.minByOrNull { it.distance() }!!
            val neighbor = loc.getWalkableNeighbor(allowSelf = false, checkForWalls = false)
            loc = neighbor ?: loc
            val localPath = LocalPathFinder.findPath(loc)
            if (localPath.isNotEmpty()) {
                localPath.traverseUntilReached()
            } else if (script.inCamTorum()) {
                walkToCamTorumMine()
            } else {
                script.logger.info("WebWalking.moveTo loc=$loc, neighbor=$neighbor")
                Movement.walkTo(loc)
            }
        }
    }
}