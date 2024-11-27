package org.powbot.krulvis.cerberus

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.KrulPaint

class CerbPainter(script: Cerberus) : KrulPaint<Cerberus>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder.build()
	}

}