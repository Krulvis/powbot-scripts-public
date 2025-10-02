package org.powbot.krulvis.cerberus

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.mobile.drawing.Rendering

class CerbPainter(script: Cerberus) : KrulPaint<Cerberus>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Cerb") { script.cerberus.healthBarVisible().toString() }
            .addString("FlinchTimer") { script.flinchTimer.getRemainderString() }
            .addString("Kills") { "${script.cerbKills}, ${script.timer.getPerHour(script.cerbKills)}/hr" }
            .build()
    }

    override fun paintCustom(g: Rendering) {
//        script.cerbTile.drawOnScreen("Cerb", outlineColor = GREEN)
//        script.centerTile.drawOnScreen("Center", outlineColor = GREEN)
//        Movement.destination().drawOnScreen("Dest", outlineColor = CYAN)
//        script.cerberus.bounds(-49, 52, -282, -220, -92, 112)
//        script.cerberus.boundingModel()?.drawWireFrame()
    }
}