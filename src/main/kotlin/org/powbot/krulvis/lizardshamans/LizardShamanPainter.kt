package org.powbot.krulvis.lizardshamans

import org.powbot.api.Color
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

class LizardShamanPainter(script: LizardShamans) : ATPaint<LizardShamans>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Kills") { perHourText(script.kills) }
			.addString("Interacting") { "${script.currentTarget.name}, valid=${script.currentTarget.valid()}" }
			.addString("LastAnimation") { script.lastAnimation.toString() }
			.addString("Last Jump") { script.jumpEvent.toString() }
			.trackSkill(Skill.Ranged)
			.build()
	}

	override fun paintCustom(g: Rendering) {
		val escapeTiles = script.escapeTiles
//		val best = escapeTiles.maxBy { it.second }
		escapeTiles.forEachIndexed { index, escapeTile ->
//			val text = Data.Direction.values()[index].name + " " + escapeTile.second
			val text = Data.Direction.values()[index].name
//			escapeTile.first.drawOnScreen(text, outlineColor = if (escapeTile == best) Color.GREEN else Color.ORANGE)
			escapeTile.first.drawOnScreen(text, outlineColor = Color.GREEN)
		}

		val currentTarget = script.currentTarget
		if (currentTarget.valid()) {
			currentTarget.tile().drawOnScreen(outlineColor = Color.RED)
		}
	}
}