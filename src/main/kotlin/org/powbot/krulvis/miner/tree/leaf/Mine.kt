package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Ore.Companion.mined
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.miner.Miner

class Mine(script: Miner) : Leaf<Miner>(script, "Mining") {

    override fun execute() {
        val rock = script.getBestRock()
        if (rock != null) {
            val path = LocalPathFinder.findPath(rock.tile().getWalkableNeighbor())
            if (path.containsSpecialNode()) {
                path.traverse()
            } else {
                setCameraPosition(rock)
                if ((script.fastMine || script.mineDelay.isFinished()) && walkAndInteract(rock, "Mine")) {
                    if (waitFor(long()) { me.animation() > 0 || rock.mined() }) {
                        script.mineDelay.resetTimer()
                    }
                }
            }
        }
    }

    fun setCameraPosition(rock: GameObject) {
        if (rock.name == "Calcified rocks") {
            val orientation = rock.orientation()
            val yaw = when (orientation) {
                4 -> 270
                5 -> 180
                6 -> 90
                7 -> 360
                else -> Camera.yaw()
            }
            script.logger.info("Setting yaw=${Camera.yaw()} to=${yaw}")
            Camera.angle(yaw, 2)
        }

    }
}