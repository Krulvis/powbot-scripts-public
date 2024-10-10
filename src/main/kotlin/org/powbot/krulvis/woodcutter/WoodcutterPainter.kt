package org.powbot.krulvis.woodcutter

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.woodcutter.tree.leaf.Burn
import org.powbot.mobile.drawing.Rendering

class WoodcutterPainter(script: Woodcutter) : ATPaint<Woodcutter>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		paintBuilder
			.trackSkill(Skill.Woodcutting)
			.trackSkill(Skill.Firemaking)
		if (script.bank) {
			paintBuilder.trackInventoryItems(
				*script.LOGS, *script.NESTS
			)
		}
		return paintBuilder.build()
	}

	override fun paintCustom(g: Rendering) {
		if (script.lastLeaf is Burn)
			script.burnTile?.drawOnScreen()
	}
}
