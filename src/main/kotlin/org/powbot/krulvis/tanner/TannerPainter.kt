package org.powbot.krulvis.tanner

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.KrulPaint

class TannerPainter(script: Tanner) : KrulPaint<Tanner>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.trackInventoryItems(*Data.Hide.values().map { it.product }.toIntArray())
			.build()
	}
}