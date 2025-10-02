package org.powbot.krulvis.cerberus.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.cerberus.Cerberus

class WalkToSpawn(script: Cerberus) : Leaf<Cerberus>(script, "Walk to spawn") {
    override fun execute() {
        val spawn = script.spawnTiles[0]
        if (spawn.distance() > 0) {
            if (Movement.stepNoConfirm(spawn)) {
                waitForDistance(spawn) { me.tile() == spawn }
            } else {
                script.logger.info("Failed to step to spawn")
            }
        }
    }
}