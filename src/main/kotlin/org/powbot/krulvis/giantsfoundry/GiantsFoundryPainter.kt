package org.powbot.krulvis.giantsfoundry

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.paint.PaintFormatters
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering
import org.powbot.mobile.script.ScriptManager

class GiantsFoundryPainter(script: GiantsFoundry) : ATPaint<GiantsFoundry>(script) {


	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		paintBuilder
			.addString("Heat") {
				"${currentTemp()}"
			}
			.trackSkill(Skill.Smithing)
			.addString("Coins") {
				"${script.coins} (${
					PaintFormatters.perHour(
						script.coins,
						ScriptManager.getRuntime(true)
					)
				}/hr)"
			}
			.addString("Action") { "${script.currentAction}" }
//            .addString("Can Perform") { "${script.currentAction?.canPerform()}" }
//            .addString("Job") { Varpbits.varpbit(JOB_VARP).toString(2) }
//            .addString("Heat") { Varpbits.varpbit(HEAT_VARP, 10, 2147483647).toString(2) }
//            .addString("Forte") { MouldType.Forte.selected().toString(2) }
//            .addString("Blades") { MouldType.Blades.selected().toString(2) }
//            .addString("Tips") { MouldType.Tips.selected().toString(2) }
//            .addString("SelectedAll") { MouldType.selectedAll().toString() }
//            .addString("OpenPage") { MouldType.openPage().toString() }
		BARS.forEachIndexed { index, bar ->
			paintBuilder.addString(bar.itemName) { "${bar.crucibleCount()}" }
		}
		return paintBuilder.build()
	}

	override fun paintCustom(g: Rendering) {

	}
}