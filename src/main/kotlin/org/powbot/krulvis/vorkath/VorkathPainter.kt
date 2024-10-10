package org.powbot.krulvis.vorkath

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class VorkathPainter(script: Vorkath) : ATPaint<Vorkath>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Kills") { perHourText(script.kills) }
			.build()
	}
}