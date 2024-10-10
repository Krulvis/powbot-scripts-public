package org.powbot.krulvis.tanner

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class TannerPainter(script: Tanner) : ATPaint<Tanner>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.trackInventoryItems(*Data.Hide.values().map { it.product }.toIntArray())
			.build()
	}
}