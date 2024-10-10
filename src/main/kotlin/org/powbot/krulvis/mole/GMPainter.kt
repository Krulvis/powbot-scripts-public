package org.powbot.krulvis.mole

import org.powbot.api.rt4.Game
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

class GMPainter(script: GiantMole) : ATPaint<GiantMole>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Last attack") {
				val splats = script.moleSplatCycles
				if (splats.isEmpty()) ""
				else (Game.cycle() - splats[0]).toString()
			}
			.addString("Kills") { "${script.kills}, ${script.timer.getPerHour(script.kills)}/hr" }
			.trackSkill(Skill.Attack)
			.trackSkill(Skill.Strength)
			.trackSkill(Skill.Defence)
			.trackInventoryItems()
			.build()
	}


}