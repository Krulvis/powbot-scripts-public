package org.powbot.krulvis.miner

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.extensions.items.Item.Companion.BLESSED_BONE_SHARD
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.script.painter.ATPaint


class MinerPainter(script: Miner) : ATPaint<Miner>(script) {

	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		paintBuilder
			.trackSkill(Skill.Mining)
			.trackInventoryItems(
				12012,
				BLESSED_BONE_SHARD,
				*Ore.values().filter { it != Ore.PAY_DIRT }.flatMap { it.ids.toList() }.toIntArray()
			)
			.withTotalLoot(true)
		return paintBuilder.build()
	}
}