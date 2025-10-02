package org.powbot.krulvis.cerberus.tree.leaf

import org.powbot.api.Input
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.cerberus.Cerberus

class StandUnder(script: Cerberus) : Leaf<Cerberus>(script, "Stand Under") {
    override fun execute() {
        val centerTile = script.centerTile
        ATContext.logger.info("stepNoConfirm interact with matrix ")
        val startTime = System.currentTimeMillis()
        val matrix = centerTile.matrix()
        val interact = if (matrix.inViewport()) {
            matrix.nextPoint().interact("Walk here")
        } else {
            val point = Game.tileToMap(centerTile)
            Input.tap(point)
        }
        ATContext.logger.info("stepNoConfirm interact with matrix, result=$interact, took=${System.currentTimeMillis() - startTime}ms")
        if (script.shouldConsume.validate()) {
            script.shouldConsume.successComponent.execute()
        }
        waitForDistance(centerTile) { me.trueTile() == centerTile }
        if (script.hasAttackOption()) {
            script.clickDoor()
        }
        if (Prayer.quickPrayer()) {
            Prayer.quickPrayer(false)
        }
    }
}