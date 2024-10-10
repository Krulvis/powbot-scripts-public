package org.powbot.krulvis.grotesqueguardians

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class GGPaint(script: GrotesqueGuardians) : ATPaint<GrotesqueGuardians>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder.build()
	}
}