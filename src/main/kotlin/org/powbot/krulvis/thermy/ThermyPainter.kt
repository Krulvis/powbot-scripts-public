package org.powbot.krulvis.thermy

import org.powbot.api.Color
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.distanceC
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.mobile.drawing.Rendering

class ThermyPainter(script: Thermy) : KrulPaint<Thermy>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Distance") { script.tt.distanceC(me.tile()).toString() }
            .addString("FreezeTimer") { script.freezeTimer.toString() }
            .build()
    }

    override fun paintCustom(g: Rendering) {
        script.tt.drawOnScreen(outlineColor = Color.RED)
        script.closestMaxDistTile.drawOnScreen(outlineColor = Color.GREEN)
    }
}