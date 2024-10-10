package org.powbot.krulvis.thiever

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Item.Companion.COINS
import org.powbot.krulvis.api.script.painter.ATPaint

class ThieverPainter(script: Thiever) : ATPaint<Thiever>(script) {

	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("Animation, Stunned") { "${me.animation()}, ${script.stunned()}" }
			.trackSkill(Skill.Thieving)
			.trackInventoryItem(COINS)
			.withTotalLoot(true)
			.build()
	}
}