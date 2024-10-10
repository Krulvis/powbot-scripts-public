package org.powbot.krulvis.tithe

import org.powbot.api.Color
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.paint.PaintFormatters
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering
import org.powbot.mobile.script.ScriptManager

class TithePainter(script: TitheFarmer) : ATPaint<TitheFarmer>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Last leaf:") { script.lastLeaf.name }
			.addString("Single-tap") { Game.singleTapEnabled().toString() }
			.trackSkill(Skill.Farming)
			.trackInventoryItems(13426, 13427, 13428)
			.addCheckbox("Last round:", "lastRound", false)
			.build()
	}

	override fun paintCustom(g: Rendering) {
		if (debugComponents) {
			script.lastPatch?.tile?.drawOnScreen(outlineColor = Color.GREEN)
			script.nextPatch?.tile?.drawOnScreen(outlineColor = Color.ORANGE)
		}
	}
}