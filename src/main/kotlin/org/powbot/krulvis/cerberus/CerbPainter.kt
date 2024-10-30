package org.powbot.krulvis.cerberus

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class CerbPainter(script: Cerberus) : ATPaint<Cerberus>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder.build()
	}

}