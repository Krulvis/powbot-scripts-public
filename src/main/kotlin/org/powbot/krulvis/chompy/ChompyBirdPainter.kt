package org.powbot.krulvis.chompy

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

class ChompyBirdPainter(script: ChompyBird) : ATPaint<ChompyBird>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Kills") { script.kills.toString() + ", " + script.timer.getPerHour(script.kills) + "/hr" }
			.build()
	}


	override fun paintCustom(g: Rendering) {
		script.placementTile.drawOnScreen()
	}

}