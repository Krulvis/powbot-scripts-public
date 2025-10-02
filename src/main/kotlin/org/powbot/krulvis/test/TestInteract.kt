package org.powbot.krulvis.test

import org.powbot.api.Color.CYAN
import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Players
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.Utils.sleep

@ScriptManifest("testInteract", "Tests interactions with objects", "Krulvis", priv = true)
class TestInteract : AbstractScript() {

    var tile = Tile.Nil
    var succeed = 0
    var total = 0
    override fun poll() {
        tile = Npcs.stream().name("Goblin").within(5).toList().random().tile()
        val interaction = tile.matrix().nextPoint().interact("Walk here")
        logger.info("Interaction result: $interaction")
        total++
        if (interaction) succeed++
        sleep(600)
    }


    override fun onStart() {
        addPaint(
            PaintBuilder.newBuilder()
                .addString("Success") { "$succeed/$total, ${(succeed.toDouble() / total * 100.0).toInt()}%" }
                .build()
        )
    }
}

fun main() {
    TestInteract().startScript()
}