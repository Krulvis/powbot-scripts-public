package org.powbot.krulvis.test

import org.powbot.api.Color
import org.powbot.api.Rectangle
import org.powbot.api.Tile
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.mobile.drawing.FrameListener
import org.powbot.mobile.drawing.FrameManager
import org.powbot.mobile.drawing.Rendering

@ScriptManifest("testInteract", "Tests interactions with objects", "Krulvis", priv = true)
class TestInteract : AbstractScript(), FrameListener {

    var tile = Tile.Nil
    var visible = false
    var bankRect = Rectangle.Nil
    override fun poll() {
        val guardian = Npcs.stream().name("", "Maze Guardian").first()
        tile = guardian.tile()
        visible = guardian.inViewport(checkIfObstructed = true)
        if (!visible) {
            logger.info("Moving camera to tile $tile")
            Camera.turnTo(tile)
        }
    }


    //-5476376657023342544
//-5476376657022799568
    override fun onStart() {
        FrameManager.addListener(this)
        addPaint(
            PaintBuilder.newBuilder()
                .addString("InViewPort") { visible.toString() }
                .addString("Zoom") { Camera.zoom.toString() }
//                .addString("Success") { "$succeed/$total, ${(succeed.toDouble() / total * 100.0).toInt()}%" }
                .build()
        )
    }

    override fun onFrame() {
        Rendering.setColor(Color.CYAN)
        Rendering.drawRect(bankRect)
    }
}

fun main() {
    TestInteract().startScript()
}