package org.powbot.krulvis.nex

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class NexPainter(script: Nex) : ATPaint<Nex>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Kills") { perHourText(script.kills) }
			.build()
	}
}