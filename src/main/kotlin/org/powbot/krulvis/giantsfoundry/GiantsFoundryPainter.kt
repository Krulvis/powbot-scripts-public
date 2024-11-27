package org.powbot.krulvis.giantsfoundry

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.paint.PaintFormatters
import org.powbot.krulvis.api.script.painter.KrulPaint
import org.powbot.mobile.drawing.Rendering
import org.powbot.mobile.script.ScriptManager

class GiantsFoundryPainter(script: GiantsFoundry) : KrulPaint<GiantsFoundry>(script) {

	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		paintBuilder
			.trackSkill(Skill.Smithing)
			.addString("Coins") {
				"${script.coins} (${
					PaintFormatters.perHour(
						script.coins,
						ScriptManager.getRuntime(true)
					)
				}/hr)"
			}
			.addString("Action") {
				val action = script.currentStage.action
				"${action}: (${action.minHeat} - ${action.maxHeat})"
			}
			.addString("Heat") { currentHeat().toString() }
			.addString("Progress") { currentProgress().toString() }
		return paintBuilder.build()
	}

	override fun paintCustom(g: Rendering) {
		val x = 10
		var y = 100
		val yy = 15
		val stages = script.stages
		val progress = currentProgress()
		stages.forEach { stage ->
			g.drawString(
				"${stage.action.name}: start=${stage.start} - end=${stage.end}, " +
					"active=${progress in stage.start..stage.end}",
				x, y
			)
			y += yy
		}
	}
}