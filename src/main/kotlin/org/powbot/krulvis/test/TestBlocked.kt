package org.powbot.krulvis.test

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.me

@ScriptManifest(name = "Test Blocked", "", priv = true)
class TestBlocked : AbstractScript() {

    var size = 1
    var calcTime = 0L

    override fun poll() {
        val me = me.trueTile()

        val flags = Movement.collisionMap(me.floor).flags()
        val startTime = System.currentTimeMillis()
        val tiles = mutableListOf<Tile>()
        for (x in -size until size) {
            for (y in -size until size) {
                tiles.add(me.derive(x, y))
            }
        }

        tiles.forEach { it.blocked(flags) }
        calcTime = System.currentTimeMillis() - startTime
        logger.info("Checking blocked of grid of $size by $size took $calcTime ms")
        size += 1
    }

    override fun onStart() {
        super.onStart()
        addPaint(
            PaintBuilder.newBuilder()
                .addString("Last calc time") { "$calcTime ms" }
                .build()
        )
    }
}

fun main() {
    TestBlocked().startScript("127.0.0.1", "GIM", true)
}