package org.powbot.krulvis.thermy.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.stepNoConfirm
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.thermy.Thermy

class WalkToMaxRange(script: Thermy) : Leaf<Thermy>(script, "WalkToMaxRange") {
    override fun execute() {
        val dest = Movement.destination()
        if (script.closestMaxDistTile != Tile.Nil) {
            script.logger.warn("Walking to best suitable tile")
            if (Movement.stepNoConfirm(script.closestMaxDistTile)) {
                waitFor { dest != Movement.destination() }
            }
        }
    }
}